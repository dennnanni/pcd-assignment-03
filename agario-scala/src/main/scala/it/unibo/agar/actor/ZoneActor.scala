package it.unibo.agar.actor

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import it.unibo.agar.model.{Coord, EatingManager, Food, Player}
import it.unibo.agar.Message

object ZoneActor:
  sealed trait Command extends Message
  final case class Init(minW: Double, maxW: Double, minH: Double, maxH: Double, coord: Coord) extends Command
  final case class EnterZone(player: Player, playerRef: ActorRef[PlayerActor.Command]) extends Command
  final case class LeaveZone(playerId: String) extends Command
  final case class AddFood(x: Double, y: Double) extends Command
  final case class MovePlayer(player: Player) extends Command
  final case class AddPlayer(player: Player) extends Command

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("ZoneActor")

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      new Zone(context).idle
    }

class Zone(
    ctx: ActorContext[ZoneActor.Command]
):

  import ZoneActor.*

  private var players: Seq[(Player, ActorRef[PlayerActor.Command])] = Seq.empty // List of players in the zone
  private var foods: Seq[Food] = Seq.empty // List of food in the zone
  private val eatingManager = EatingManager

  var minW: Double = 0.0
  var maxW: Double = 0.0
  var minH: Double = 0.0
  var maxH: Double = 0.0
  var coord: Coord = Coord(0, 0)

  val idle: Behavior[Command] =
    Behaviors.receiveMessage {
      case Init(
        minW: Double,
        maxW: Double,
        minH: Double,
        maxH: Double,
        coord: Coord
      ) =>
        this.minW = minW
        this.maxW = maxW
        this.minH = minH
        this.maxH = maxH
        this.coord = coord
        active
      case msg =>
        ctx.log.warn(s"Zone zone-${coord.x}-${coord.y} Received unexpected message in idle state: $msg")
        Behaviors.unhandled
    }

  val active: Behavior[Command] =
    Behaviors.receiveMessage {
      case AddPlayer(player) =>
        ctx.log.info(s"Player ${player.id} added to zone at $coord")
        players = players ++ Seq((player, null))
        Behaviors.same
      case EnterZone(player, playerRef) =>
        ctx.log.info(s"Player ${player.id} entered zone-${coord.x}-${coord.y} ")
        players ++= Seq((player, playerRef))
        Behaviors.same
      case LeaveZone(playerId) =>
        players = players.filterNot(_._1.id == playerId)
        Behaviors.same
      case AddFood(x, y) =>
        ctx.log.info(s"Food added to zone at $coord")

        foods = foods ++ Seq(Food(
          id = s"food-${java.util.UUID.randomUUID()}",
          x = x,
          y = y
        ))
      players.foreach { case (_, ref) =>
        if ref != null then
          ref ! PlayerActor.UpdateWorld(coord, players.map(_._1), foods)
      }
        Behaviors.same
      case MovePlayer(player) =>
        players.find(_._1.id == player.id) match
          case Some((_, playerRef)) =>
            players = players.map {
              case (p, ref) if p.id == player.id => (player, ref)
              case other => other
            }

            val (playersEaten, foodEaten) = getEatenEntities(player)
            val entitiesEaten = foodEaten ++ playersEaten.map(_._1)
            if playersEaten.nonEmpty then
              players = players.filterNot(p => playersEaten.map(_._1.id).contains(p._1.id))
              playersEaten.foreach { case (p, ref) => if ref != null then
                ref ! PlayerActor.RemovePlayer(p.id)
              }
            if foodEaten.nonEmpty then
              foods = foods.filterNot(food => foodEaten.map(_.id).contains(food.id))
            if entitiesEaten.nonEmpty then
              playerRef ! PlayerActor.Grow(entitiesEaten)

            players.foreach { case (_, ref) => if ref != null then {
              ref ! PlayerActor.UpdateWorld(coord, players.map(_._1), foods)
            }}
          case _ =>
            ctx.log.warn(s"zone-${coord.x}-${coord.y} => Player ${player.id} not found in zone at $coord")
          Behaviors.same
    }

  private def getEatenEntities(player: Player): (Seq[(Player, ActorRef[PlayerActor.Command])], Seq[Food]) =
    val foodEaten = foods.filter(food => EatingManager.canEatFood(player, food))
    val playersEaten = players
      .filterNot(_._1.id == player.id)
      .filter(p => EatingManager.canEatPlayer(player, p._1))
    (playersEaten, foodEaten)
