package it.unibo.agar.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.agar.model.Coord
import it.unibo.agar.view.LocalView

object PlayerActor:
  sealed trait Command
  final case class Init(zoneManager: ActorRef[ZoneManager.Command]) extends Command
  final case class MoveTo(x: Double, y: Double) extends Command
  final case class ZoneLocated(coord: Coord, zone: ActorRef[ZoneActor.Command]) extends Command

  def apply(playerId: String): Behavior[Command] =
    Behaviors.setup { context =>
      new Player(context, playerId).idle
    }

class Player(
              context: ActorContext[PlayerActor.Command],
              playerId: String
            ):
  import PlayerActor.*

  private var zoneManager: ActorRef[ZoneManager.Command] = null
  private var currentZone: Option[ActorRef[ZoneActor.Command]] = None

  def idle: Behavior[Command] = Behaviors.receiveMessage {
    case Init(zm) =>
      context.log.info(s"$playerId initialized")
      zoneManager = zm
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

    case _ => Behaviors.unhandled
  }

  val replyAdapter: ActorRef[ZoneManager.ZoneRef] =
    context.messageAdapter { case ZoneManager.ZoneRef(coord, ref) =>
      ZoneLocated(coord, ref)
    }

  // Devi fornire il riferimento al zoneManager esternamente
