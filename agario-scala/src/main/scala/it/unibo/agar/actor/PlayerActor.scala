package it.unibo.agar.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import it.unibo.agar.model.{Coord, Food, Player, Position, ViewWorld}
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
  private var currentZones: Seq[ActorRef[ZoneActor.Command]] = Seq.empty
  private val view: LocalView = LocalView(playerId, (x: Double, y: Double) =>
    context.self ! MoveTo(x, y)
  )
  private var player: Player = Player(playerId, 500, 500, 100.0) // Initial position and mass

  def idle: Behavior[Command] = Behaviors.receiveMessage {
    case Init(zm) =>
      context.log.info(s"$playerId initialized")
      zoneManager = zm
      onEDT {
        view.updatePlayer(player) // TODO: randomize initial position
        view.open()
      }
      val zones: Seq[Position] = player.computeSightLimit(1000, 1000) // TODO: use world size
      zones.foreach { zone =>
        zm ! ZoneManager.GetZone(zone.x, zone.y, replyAdapter)
      }
      moving(500, 500) // Start in the center of the world
    case _ => Behaviors.same
  }

  def moving(x: Double, y: Double): Behavior[Command] = Behaviors.receiveMessage {
    case MoveTo(newX, newY) =>
      context.log.info(s"$playerId moving to ($newX, $newY)")
      val newPosition = Position(newX, newY)
      player = player.copy(x = player.x + newX, y = player.y + newY) // Update player position
      view.updatePlayer(player)
      zoneManager ! ZoneManager.GetZone(newX, newY, replyAdapter)
      Behaviors.same
    case ZoneLocated(_, zone) =>
      zone ! ZoneActor.AddFood()
      zone ! ZoneActor.EnterZone(player, context.self)
      currentZones = currentZones.appended(zone)
      Behaviors.same
    case UpdateWorld(players, foods) =>
      view.updateWorld(ViewWorld(players, foods))
      Behaviors.same

    case _ => Behaviors.unhandled
  }

  val replyAdapter: ActorRef[ZoneManager.ZoneRef] =
    context.messageAdapter { case ZoneManager.ZoneRef(coord, ref) =>
      ZoneLocated(coord, ref)
    }

