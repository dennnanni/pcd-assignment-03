package it.unibo.agar.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import it.unibo.agar.Message
import it.unibo.agar.model.{Coord, Food, MockGameStateManager, Player, World}
import it.unibo.agar.view.GlobalView

import scala.swing.Swing.onEDT

object GlobalViewActor:
  sealed trait Command extends Message
  final case class UpdateWorld(zone: Coord, players: Seq[Player], food: Seq[Food]) extends Command
  final case class GameOver(playerId: String) extends Command

  val stateManager = MockGameStateManager(World.empty)
  val view = GlobalView(stateManager)

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("GlobalViewActor")

  def apply(id: String): Behavior[Command] = {
    Behaviors.setup { context =>
      context.log.info(s"GlobalViewActor started with id: $id")
      stateManager.getWorld.getGrid.allCoords.foreach { coord =>
        val zoneActor = ClusterSharding(context.system)
          .entityRefFor(ZoneActor.TypeKey, s"zone-${coord.x}-${coord.y}")
        zoneActor ! ZoneActor.Subscribe(id)
      }

      Behaviors.receiveMessage[Command] {
        case UpdateWorld(zone, players, food) =>
          onEDT:
            if !view.visible then view.open()
          stateManager.updateWorld(zone, players, food)
          onEDT:
            view.repaint()
          Behaviors.same
        case GameOver(playerId) =>
          context.log.info(s"Game over for player $playerId.")
          view.showGameOver(s"$playerId won.")
          Behaviors.stopped
      }
    }
  }