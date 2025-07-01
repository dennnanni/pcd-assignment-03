package it.unibo.agar.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import it.unibo.agar.model.{Coord, Entity, Food, LocalGameStateManager, Player}
import it.unibo.agar.view.LocalView
import it.unibo.agar.Message

import javax.swing.SwingUtilities
import scala.concurrent.duration.DurationInt
import scala.swing.Swing.onEDT
import scala.util.Random


object PlayerActor:
  sealed trait Command extends Message
  final case class Init(width: Double, height: Double, zones: Map[Coord, ActorRef[ZoneActor.Command]]) extends Command
  final case class Tick() extends Command
  final case class UpdateWorld(zone: Coord, players: Seq[Player], food: Seq[Food]) extends Command
  final case class Grow(entities: Seq[Entity]) extends Command
  final case class RemovePlayer(playerId: String) extends Command

  def apply(playerId: String): Behavior[Command] =
    Behaviors.withTimers { timers =>
      Behaviors.setup { context =>
        timers.startTimerAtFixedRate("positionUpdate", Tick(), 100.milli, 30.milli) // Adjust the duration as needed
        new PlayerEntity(context, playerId).idle
      }
    }

class PlayerEntity(
    context: ActorContext[PlayerActor.Command],
    playerId: String
):
  import PlayerActor.*

  private var stateManager = LocalGameStateManager.empty // Initial position and mass
  private var currentZones: Map[Coord, EntityRef[ZoneActor.Command]] = Map.empty
  private var allZones: Map[Coord, ActorRef[ZoneActor.Command]] = Map.empty
  private var view = LocalView.empty

  val idle: Behavior[Command] = Behaviors.receiveMessage {
    case Init(width, height, zones) =>
      allZones = zones
      stateManager = LocalGameStateManager(
        Player(
          playerId,
          Random.between(0, width),
          Random.between(0, height),
          120.0
        ),
        width,
        height,
      )
      view = LocalView(
        stateManager,
        playerId
      )
      onEDT {
        view.open()
      }
      context.self ! Tick()
      moving
    case msg =>
      context.log.warn(s"$playerId received unexpected message in idle state $msg")
      Behaviors.same
  }

  private def moving: Behavior[Command] = Behaviors.receiveMessage {
    case UpdateWorld(zone, players, food) =>
      stateManager.updateWorld(players.filterNot(p => p.id == playerId), zone, food)
      Behaviors.same
    case Tick() =>
      val coord = stateManager.getPlayerSightLimit.map(p => stateManager.getCoord(p._1, p._2)).toSet
      currentZones.filterNot { case (c, _) => coord.contains(c) }.foreach { case (_, z) =>
        z ! ZoneActor.LeaveZone(playerId)
      }
      currentZones = currentZones.filter { case (c, _) => coord.contains(c) }
      coord.diff(currentZones.keySet).foreach { c =>
        val zoneActor = ClusterSharding(context.system)
          .entityRefFor(ZoneActor.TypeKey, s"zone-${c._1}-${c._2}")
        zoneActor ! ZoneActor.EnterZone(stateManager.getPlayer, context.self)
        currentZones += (c -> zoneActor)
      }

      stateManager.tick()
      currentZones.foreach { case (_, zoneActor) =>
        zoneActor ! ZoneActor.MovePlayer(stateManager.getPlayer)
      }
      onEDT {
        stateManager.copyWorld
        view.repaint()
      }
      Behaviors.same
    case Grow(entities) =>
      val mass = entities.foldLeft(0.0)((acc, entity) => acc + entity.mass)
      SwingUtilities.invokeLater(() =>
        stateManager.player = stateManager.player.grow(mass)
      )
      Behaviors.same
    case RemovePlayer(playerId) =>
      SwingUtilities.invokeLater(() =>
        view.showGameOver(s"Player $playerId has been removed from the game.")
      )
      Behaviors.stopped
  }
