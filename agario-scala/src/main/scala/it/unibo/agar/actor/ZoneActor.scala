package it.unibo.agar.actor


import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.agar.model.{Coord, Food, Player}

import scala.util.Random


object ZoneActor:
  // Messaggi
  sealed trait Command
  final case class EnterZone(playerId: Player, player: ActorRef[PlayerActor.Command]) extends Command
  final case class LeaveZone(playerId: String) extends Command
  final case class AddFood() extends Command

  def apply(minW: Double, maxW: Double, minH: Double, maxH: Double, coord: Coord): Behavior[Command] = Behaviors.setup { context =>
    new Zone(context, minW, maxW, minH, maxH, coord).active
  }

class Zone(
    ctx: ActorContext[ZoneActor.Command],
    minW: Double,
    maxW: Double,
    minH: Double,
    maxH: Double,
    coord: Coord
):
  import ZoneActor.*

  var players: Seq[(Player, ActorRef[PlayerActor.Command])] = Seq.empty // Lista dei giocatori presenti nella zona
  var foods: Seq[Food] = Seq.empty // Lista del cibo presente nella zona

  val active: Behavior[Command] =
    Behaviors.receiveMessage:
      case EnterZone(player, playerRef) =>
        ctx.log.info(s"Player ${player.id} entered zone with bounds ($minW, $maxW, $minH, $maxH)")
        players = players.appended(player, playerRef)
        Behaviors.same
      case LeaveZone(playerId) =>
        ctx.log.info(s"Player $playerId left zone with bounds ($minW, $maxW, $minH, $maxH)")
        players = players.filterNot(_._1.id == playerId)
        Behaviors.same
      case AddFood() =>
        val food = Food(
          id = s"food-${Random.alphanumeric.take(8).mkString}",
          x = Random.between(minW, maxW),
          y = Random.between(minH, maxH)
        )
        ctx.log.info(s"Adding food ${food.id} to zone with bounds ($minW, $maxW, $minH, $maxH)")
        foods = foods.appended(food)
        Behaviors.same
