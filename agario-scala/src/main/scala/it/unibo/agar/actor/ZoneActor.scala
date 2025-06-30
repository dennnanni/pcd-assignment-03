package it.unibo.agar.actor

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.model.{Coord, Food, Player}

object ZoneActor:
  sealed trait Command
  final case class EnterZone(player: Player, playerRef: ActorRef[PlayerActor.Command]) extends Command
  final case class LeaveZone(playerId: String) extends Command
  final case class AddFood(x: Double, y: Double) extends Command

  def apply(minW: Double, maxW: Double, minH: Double, maxH: Double, coord: Coord): Behavior[Command] =
    Behaviors.setup { context =>
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

  private var players: Seq[(Player, ActorRef[PlayerActor.Command])] = Seq.empty // List of players in the zone
  private var foods: Seq[Food] = Seq.empty // List of food in the zone

  val active: Behavior[Command] =
    Behaviors.receiveMessage {
      case EnterZone(player, playerRef) =>
        players ++= Seq((player, playerRef))
        Behaviors.same
      case LeaveZone(playerId) =>
        players = players.filterNot(_._1.id == playerId)
        Behaviors.same
      case AddFood(x, y) =>
        foods = foods ++ Seq(Food(
          id = s"food-${java.util.UUID.randomUUID()}",
          x = x,
          y = y
        ))
        players.foreach { case (player, playerRef) =>
          playerRef ! PlayerActor.UpdateWorld(coord, players.map(_._1), foods)
        }
        Behaviors.same
    }
