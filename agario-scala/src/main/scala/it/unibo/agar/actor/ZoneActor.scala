package it.unibo.agar.actor

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import it.unibo.agar.model.{Coord, EatingManager, Food, Player}
import it.unibo.agar.Message

private sealed trait ZoneEvent extends Message
private final case class ZoneInitialized(config: ZoneConfig) extends ZoneEvent
private final case class PlayerAdded(player: Player) extends ZoneEvent
private final case class PlayerRemoved(playersId: Seq[String]) extends ZoneEvent
private final case class FoodAdded(food: Food) extends ZoneEvent
final case class FoodRemoved(uids: Seq[String]) extends ZoneEvent
private final case class PlayerMoved(player: Player) extends ZoneEvent

final case class ZoneState(config: Option[ZoneConfig],
                           players: Seq[Player] = Seq.empty,
                           foods: Seq[Food] = Seq.empty,
                           initialized: Boolean = false) {
  def applyEvent(event: ZoneEvent): ZoneState = event match {
    case ZoneInitialized(c) =>
      copy(config = Some(c), initialized = true)
    case PlayerAdded(player) =>
      copy(players = players ++ Seq(player))
    case PlayerRemoved(playersId) =>
      copy(players = players.filterNot(p => playersId.contains(p.id)))
    case FoodAdded(food) =>
      copy(foods = foods ++ Seq(food))
    case FoodRemoved(uids) =>
      copy(foods = foods.filterNot(f => uids.contains(f.id)))
    case PlayerMoved(player) =>
      copy(players = players.map {
        case p if p.id == player.id => player
        case other => other
      })
  }
}

object ZoneState {
  val empty: ZoneState = ZoneState(None)
}


case class ZoneConfig(
    minW: Double,
    maxW: Double,
    minH: Double,
    maxH: Double,
    coord: Coord
)

object ZoneActor:
  sealed trait Command extends Message
  final case class Init(config: ZoneConfig) extends Command
  final case class EnterZone(player: Player, playerRef: ActorRef[PlayerActor.Command]) extends Command
  final case class LeaveZone(playerId: String) extends Command
  final case class AddFood(x: Double, y: Double) extends Command
  final case class MovePlayer(player: Player, ref: ActorRef[PlayerActor.Command]) extends Command

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("ZoneActor")

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      val zone = new Zone()
      EventSourcedBehavior[Command, ZoneEvent, ZoneState](
        persistenceId = PersistenceId.ofUniqueId(context.self.path.name),
        emptyState = ZoneState.empty,
        commandHandler = (state, cmd) => {
          val effect = commandHandler(context, zone, state, cmd)
          zone.currentState = state
          effect
        },
        eventHandler = (state, event) => state.applyEvent(event)
      )
    }


  private def commandHandler(
                              ctx: ActorContext[Command],
                              zone: Zone,
                              state: ZoneState,
                              command: Command
                            ): Effect[ZoneEvent, ZoneState] =
    import ctx.log

    command match {
      case Init(config) =>
        if state.initialized then
          log.info("Zone already initialized, ignoring Init")
          Effect.none
        else
          log.info(s"Persisting ZoneInitialized for config: $config")
          Effect.persist(ZoneInitialized(config))
      case EnterZone(player, playerRef) =>
        state.config match {
          case Some(config) =>
            Effect.persist(PlayerAdded(player)).thenRun { _ =>
              zone.addPlayerRef(player.id, playerRef)
            }
          case None =>
            log.warn("Zone not initialized yet, ignoring EnterZone")
            Effect.unhandled
        }
      case LeaveZone(playerId) =>
        state.config match {
          case Some(config) =>
            Effect.persist(PlayerRemoved(Seq(playerId))).thenRun { _ =>
              zone.removeRef(playerId)
            }
          case None =>
            log.warn("Zone not initialized yet, ignoring EnterZone")
            Effect.unhandled
        }
      case AddFood(x, y) =>
        state.config match {
          case Some(config) =>
            val food = Food(
              id = s"food-${java.util.UUID.randomUUID()}",
              x = x,
              y = y
            )
            state.players.map(p => zone.getRef(p.id)).foreach {
              case Some(ref) =>
                ref ! PlayerActor.UpdateWorld(config.coord, state.players, state.foods)
              case None =>
                log.warn(s"Player reference not found for player in zone ${config.coord}")
            }
            Effect.persist(FoodAdded(food))
          case None =>
            log.warn("Zone not initialized yet, ignoring EnterZone")
            Effect.unhandled
        }
      case MovePlayer(player, ref) =>
        state.config match {
          case Some(config) =>
            state.players.find(_.id == player.id) match
              case Some(p) =>
                if zone.getRef(player.id).isEmpty then {
                  zone.addPlayerRef(player.id, ref)
                }

                val (playersEaten, foodEaten) = getEatenEntities(player, state.foods, state.players)
                if playersEaten.nonEmpty then
                  playersEaten.foreach { p => if zone.playersRef.contains(p.id) then
                    zone.getRef(p.id).get ! PlayerActor.RemovePlayer(p.id)
                  }
                if (playersEaten.nonEmpty || foodEaten.nonEmpty) && isInBound(player, config) then
                  ref ! PlayerActor.Grow(foodEaten, playersEaten)

                val players = state.players.filterNot(p => playersEaten.contains(p.id))
                val foods = state.foods.filterNot(food => foodEaten.contains(food.id))
                players.foreach { p => if zone.playersRef.contains(p.id) then
                    zone.getRef(p.id).get ! PlayerActor.UpdateWorld(config.coord, players, foods)
                }
                Effect.persist(Seq(PlayerMoved(player), PlayerRemoved(playersEaten.map(_.id)), FoodRemoved(foodEaten.map(_.id))))
              case _ =>
                ctx.log.warn(s"zone-${config.coord.x}-${config.coord.y} => Player ${player.id} not found")
                Effect.unhandled
          case None =>
            log.warn("Zone not initialized yet, ignoring EnterZone")
            Effect.unhandled
        }
    }

  private def getEatenEntities(player: Player, foods: Seq[Food], players: Seq[Player]
                              ): (Seq[Player], Seq[Food]) =
    val foodEaten = foods.filter(food => EatingManager.canEatFood(player, food))
    val playersEaten = players
      .filterNot(_.id == player.id)
      .filter(p => EatingManager.canEatPlayer(player, p))
    (playersEaten, foodEaten)

  private def isInBound(player: Player, config: ZoneConfig): Boolean =
    player.x >= config.minW && player.x < config.maxW && player.y >= config.minH && player.y < config.maxH


class Zone():
  var currentState: ZoneState = ZoneState.empty
  var playersRef: Map[String, ActorRef[PlayerActor.Command]] = Map.empty

  def addPlayerRef(playerId: String, ref: ActorRef[PlayerActor.Command]): Unit =
    playersRef += (playerId -> ref)

  def getRef(playerId: String): Option[ActorRef[PlayerActor.Command]] =
    playersRef.get(playerId)

  def removeRef(playerId: String): Unit =
    playersRef -= playerId