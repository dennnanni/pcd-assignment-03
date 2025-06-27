package it.unibo.agar.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.model.{Coord, WorldGrid}

object ZoneManager:
  sealed trait Command
  final case class GetZone(x: Double, y: Double, replyTo: ActorRef[ZoneRef]) extends Command
  final case class ZoneRef(coord: Coord, ref: ActorRef[ZoneActor.Command]) extends Command

  def apply(worldWidth: Double, worldHeight: Double, cellSize: Double): Behavior[Command] =
    Behaviors.setup { context =>
      val grid = new WorldGrid(worldWidth, worldHeight, cellSize)

      val zones: Map[Coord, ActorRef[ZoneActor.Command]] =
        grid.allCoords.map { coord =>
          val (minW, maxW, minH, maxH) = grid.boundsOf(coord)
          val ref = context.spawn(
            ZoneActor(minW, maxW, minH, maxH, coord),
            s"zone-${coord.x}-${coord.y}"
          )
          context.log.info("Created zone actor for coord: {}", coord)
          coord -> ref
        }.toMap

      Behaviors.receiveMessage {
        case GetZone(x, y, replyTo) =>
          val coord = grid.coordFor(x, y)
          context.log.info("Received request for zone at coord: {}", coord)
          zones.get(coord).foreach { ref =>
            replyTo ! ZoneRef(coord, ref)
          }
          Behaviors.same
      }
    }

