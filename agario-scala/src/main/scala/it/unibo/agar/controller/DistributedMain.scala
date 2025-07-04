package it.unibo.agar.controller

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import it.unibo.agar.actor.{FoodGeneratorActor, GlobalViewActor, PlayerActor, ZoneActor, ZoneConfig}
import it.unibo.agar.model.WorldGrid
import it.unibo.agar.{startup, startupWithSeeds}

import java.time.InstantSource.system
import scala.swing.{Frame, SimpleSwingApplication}

private val WIDTH = 1000
private val HEIGHT = 1000
private val CELL_SIZE = 400

class DistributedAgarIo(playerId: String) extends SimpleSwingApplication:

  private val grid = WorldGrid(WIDTH, HEIGHT, CELL_SIZE)

  val playerSystem: ActorSystem[Nothing] = startupWithSeeds("agario", 0, List(25251, 25252)) {
    Behaviors.setup[Nothing] { context =>
      val sharding = ClusterSharding(context.system)

      sharding.init(Entity(ZoneActor.TypeKey) { entityContext =>
        ZoneActor()
      })

      sharding.init(Entity(GlobalViewActor.TypeKey) { entityContext =>
          GlobalViewActor("global-view")
      })

      sharding.init(Entity(FoodGeneratorActor.TypeKey) { entityContext =>
        FoodGeneratorActor(grid.width, grid.height, grid.cellSize)
      })
      Behaviors.empty
    }
  }

  Thread.sleep(5000)

  val player: ActorRef[PlayerActor.Command] = playerSystem.systemActorOf(
    PlayerActor(playerId),
    playerId
  )

  override def top: Frame = {
    player ! PlayerActor.Init(1000, 1000, Map.empty)
    new Frame {visible = false}
  }


object MainApp {
  def main(args: Array[String]): Unit = {
    val playerId = if (args.nonEmpty) args(0) else "player1"
    new DistributedAgarIo(playerId).main(args)
    Thread.currentThread().join()
  }
}

object ZonesMain:

  private val foodGenerator = 3

  def main(args: Array[String]): Unit =
    val ports = List(25251, 25252, 0)
    val grid = WorldGrid(WIDTH, HEIGHT, CELL_SIZE)

    val actorSystems = ports.map { port =>
      startup("agario", port) {
        Behaviors.setup[Nothing] { context =>
          val sharding = ClusterSharding(context.system)

          sharding.init(Entity(ZoneActor.TypeKey) { entityContext =>
            ZoneActor()
          })

          sharding.init(Entity(GlobalViewActor.TypeKey) { entityContext =>
            GlobalViewActor("global-view")
          })

          sharding.init(Entity(FoodGeneratorActor.TypeKey) { entityContext =>
            FoodGeneratorActor(grid.width, grid.height, grid.cellSize)
          })
          Behaviors.empty
        }
      }
    }

    Thread.sleep(10000)

    var i: Int = 0
    val refs = grid.allCoords.map { coord =>
      val zoneRef = ClusterSharding(actorSystems(i))
        .entityRefFor(ZoneActor.TypeKey, s"zone-${coord.x}-${coord.y}")
      i = (i + 1) % actorSystems.size
      (coord, zoneRef)
    }

    refs.foreach { (c, ref) =>
      val (minW, maxW, minH, maxH) = grid.boundsOf(c)
      ref ! ZoneActor.Init(ZoneConfig(grid.width, grid.height, minW, maxW, minH, maxH, c))
    }

    Seq.range(0, foodGenerator).foreach { i =>
      ClusterSharding(actorSystems.head)
        .entityRefFor(FoodGeneratorActor.TypeKey, s"food-generator-$i") ! FoodGeneratorActor.AddFood()
    }


class Global() extends SimpleSwingApplication:

  private val grid = WorldGrid(WIDTH, HEIGHT, CELL_SIZE)

  val globalSystem: ActorSystem[Nothing] = startupWithSeeds("agario", 0, List(25251, 25252)) {
    Behaviors.setup[Nothing] { context =>
      val sharding = ClusterSharding(context.system)

      sharding.init(Entity(ZoneActor.TypeKey) { entityContext =>
        ZoneActor()
      })

      sharding.init(Entity(GlobalViewActor.TypeKey) { entityContext =>
        GlobalViewActor("global-view")
      })

      sharding.init(Entity(FoodGeneratorActor.TypeKey) { entityContext =>
        FoodGeneratorActor(grid.width, grid.height, grid.cellSize)
      })
      Behaviors.empty
    }
  }

  Thread.sleep(5000)

  val id = "global-view"

  val globalView: ActorRef[GlobalViewActor.Command] = globalSystem.systemActorOf(
    GlobalViewActor(id),
    id
  )

  override def top: Frame = {
    new Frame {visible = false}
  }


object GlobalViewMain {
  def main(args: Array[String]): Unit = {
    new Global()
    Thread.currentThread().join()
  }
}