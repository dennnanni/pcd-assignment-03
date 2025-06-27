package it.unibo.agar.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.agar.model.{Coord, Food, Player, ViewWorld}
import it.unibo.agar.view.LocalView

import scala.swing.Swing.onEDT

object PlayerActor:
  sealed trait Command
  final case class Init(zoneManager: ActorRef[ZoneManager.Command]) extends Command
  final case class MoveTo(x: Double, y: Double) extends Command
  final case class ZoneLocated(coord: Coord, zone: ActorRef[ZoneActor.Command]) extends Command
  final case class UpdateWorld(players: Seq[Player], food: Seq[Food]) extends Command

  def apply(playerId: String): Behavior[Command] =
    Behaviors.setup { context =>
      new PlayerEntity(context, playerId).idle
    }

class PlayerEntity(
              context: ActorContext[PlayerActor.Command],
              playerId: String
            ):
  import PlayerActor.*

  private var zoneManager: ActorRef[ZoneManager.Command] = null
  private var currentZone: Option[ActorRef[ZoneActor.Command]] = None
  private val view: LocalView = LocalView(playerId)

  def idle: Behavior[Command] = Behaviors.receiveMessage {
    case Init(zm) =>
      context.log.info(s"$playerId initialized")
      zoneManager = zm
      onEDT {
        view.showPlayer(Player(playerId, 500, 500, 100.0)) // Initial position and mass
        view.open()
      }
      moving(0.0, 0.0) // Start in the center of the world
    case _ => Behaviors.same
  }

  def moving(x: Double, y: Double): Behavior[Command] = Behaviors.receiveMessage {
    case MoveTo(x, y) =>
      context.log.info(s"$playerId moving to ($x, $y)")
      zoneManager ! ZoneManager.GetZone(x, y, replyAdapter)
      Behaviors.same
    case ZoneLocated(_, zone) =>
      currentZone.foreach(_ ! ZoneActor.LeaveZone(playerId))
      zone ! ZoneActor.EnterZone(playerId)
      currentZone = Some(zone)
      Behaviors.same
    case UpdateWorld(players, foods) =>
      currentZone match {
        case Some(zone) =>
          view.updateWorld(ViewWorld(players, foods))
        case None =>
          context.log.warn(s"$playerId is not in any zone, cannot update view")
      }
      Behaviors.same

    case _ => Behaviors.unhandled
  }

  val replyAdapter: ActorRef[ZoneManager.ZoneRef] =
    context.messageAdapter { case ZoneManager.ZoneRef(coord, ref) =>
      ZoneLocated(coord, ref)
    }

  // Devi fornire il riferimento al zoneManager esternamente
