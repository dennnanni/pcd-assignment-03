package it.unibo.agar.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import it.unibo.agar.Message
import it.unibo.agar.model.WorldGrid

import scala.concurrent.duration.DurationInt
import scala.util.Random

object FoodGeneratorActor:
  sealed trait Command extends Message
  final case class AddFood() extends Command

  val TypeKey: EntityTypeKey[FoodGeneratorActor.Command] = EntityTypeKey[FoodGeneratorActor.Command]("FoodGenerator")

  def apply(width: Double, height: Double, cellsize: Double): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.withTimers { timers =>
        context.log.info("FoodGeneratorActor started")
        timers.startTimerAtFixedRate("food-generation", AddFood(), 3.second)
        new FoodGenerator(context, width, height, cellsize).foodGeneration
      }
    }

class FoodGenerator(
                   context: ActorContext[FoodGeneratorActor.Command],
                   width: Double,
                   height: Double,
                   cellsize: Double) {
  import FoodGeneratorActor._

  private val grid: WorldGrid = WorldGrid(width, height, cellsize)

  def foodGeneration: Behavior[Command] = Behaviors.receiveMessage {
    case AddFood() =>
      println("Generating food")
      val (x, y) = (Random.between(0, width), Random.between(0, height))
      val coord = grid.coordFor(x, y)
      val zone = ClusterSharding(context.system).
        entityRefFor(ZoneActor.TypeKey, s"zone-${coord.x}-${coord.y}")
      zone ! ZoneActor.AddFood(x, y)
      Behaviors.same
  }
}