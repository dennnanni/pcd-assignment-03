package it.unibo.agar.actor


import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}


object ZoneActor:
  // Messaggi
  sealed trait Command
  final case class EnterZone(playerId: String) extends Command
  final case class LeaveZone(playerId: String) extends Command

  def apply(minW: Double, maxW: Double, minH: Double, maxH: Double): Behavior[Command] = Behaviors.setup { context =>
    new Zone(context, minW, maxW, minH, maxH).active
  }

class Zone(
    ctx: ActorContext[ZoneActor.Command],
    minW: Double,
    maxW: Double,
    minH: Double,
    maxH: Double
):

  import ZoneActor.*

  val active: Behavior[Command] =
    Behaviors.receiveMessage:
      case EnterZone(playerId) =>
          ctx.log.info(s"Player $playerId entered zone with bounds ($minW, $maxW, $minH, $maxH)")
          Behaviors.same
      case LeaveZone(playerId) =>
          ctx.log.info(s"Player $playerId left zone with bounds ($minW, $maxW, $minH, $maxH)")
          Behaviors.same
