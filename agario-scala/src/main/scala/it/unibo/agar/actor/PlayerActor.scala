package it.unibo.agar.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.agar.model.{Coord, Food, LocalGameStateManager, Player}
import it.unibo.agar.view.LocalView

import scala.concurrent.duration.DurationInt
import scala.swing.Swing.onEDT
import scala.util.Random


object PlayerActor:
  sealed trait Command
  final case class Init(width: Double, height: Double, zones: Map[Coord, ActorRef[ZoneActor.Command]]) extends Command
  final case class Tick() extends Command
  final case class UpdateWorld(zone: Coord, players: Seq[Player], food: Seq[Food]) extends Command

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
  private var currentZones: Map[Coord, ActorRef[ZoneActor.Command]] = Map.empty
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
          100.0
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
      onEDT {
        stateManager.updateWorld(players, food)
      }
      Behaviors.same
    case Tick() =>
      val coord = stateManager.getPlayerSightLimit.map(p => stateManager.getCoord(p._1, p._2)).toSet
      currentZones.filterNot { case (c, _) => coord.contains(c) }.foreach { case (_, z) =>
        z ! ZoneActor.LeaveZone(playerId)
      }
      currentZones = currentZones.filter { case (c, _) => coord.contains(c) }
      coord.diff(currentZones.keySet).foreach { c =>
        allZones.get(c) match {
          case Some(zoneActor) =>
            zoneActor ! ZoneActor.EnterZone(stateManager.getPlayer, context.self)
            currentZones += (c -> zoneActor)
          case None =>
            context.log.warn(s"$playerId could not find zone at ${c._1}, ${c._2}")
        }
      }
      onEDT {
        stateManager.tick()
        view.repaint()
      } // TODO: non siamo sicuri
      Behaviors.same
  }
