package it.unibo.agar.controller

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import it.unibo.agar.actor.{PlayerActor, ZoneActor, ZoneConfig}
import it.unibo.agar.model.{Player, WorldGrid}
import it.unibo.agar.startup

import scala.swing.{Frame, SimpleSwingApplication}

class DistributedAgarIo extends SimpleSwingApplication:

  val playerSystem: ActorSystem[Nothing] = it.unibo.agar.startupWithSeeds("agario", 0, List(25251, 25252)) {
    Behaviors.setup[Nothing] { context =>
      val sharding = ClusterSharding(context.system)

      sharding.init(Entity(ZoneActor.TypeKey) { entityContext =>
        ZoneActor()
      })
      Behaviors.empty
    }
  }

  println("ACTOR SYSTEM DEL PLAYER " + playerSystem.address)

  Thread.sleep(5000)

  val player: ActorRef[PlayerActor.Command] = playerSystem.systemActorOf(
    PlayerActor("player1"),
    "player1"
  )

  override def top: Frame = {
    player ! PlayerActor.Init(1000, 1000, Map.empty)
    new Frame {visible = false}
  }


object MainApp {
  def main(args: Array[String]): Unit = {
    new DistributedAgarIo().main(args)
    Thread.currentThread().join()
  }
}

object ZonesMain:
  def main(args: Array[String]): Unit =
    val ports = List(25251, 25252, 0)

    val actorSystems = ports.map { port =>
      startup("agario", port) {
        Behaviors.setup[Nothing] { context =>
          val sharding = ClusterSharding(context.system)

          sharding.init(Entity(ZoneActor.TypeKey) { entityContext =>
            ZoneActor()
          })
          Behaviors.empty
        }
      }
    }

    Thread.sleep(10000)

    val grid = WorldGrid(1000, 1000, 400)
    var i: Int = 0
    val refs = grid.allCoords.map { coord =>
      val (minW, maxW, minH, maxH) = grid.boundsOf(coord)
      println(s"zone-${coord.x}-${coord.y}")
      val zoneRef = ClusterSharding(actorSystems(i))
        .entityRefFor(ZoneActor.TypeKey, s"zone-${coord.x}-${coord.y}")
      i = (i + 1) % actorSystems.size
      (coord, zoneRef)
    }

    refs.foreach { (c, ref) =>
      val (minW, maxW, minH, maxH) = grid.boundsOf(c)
      ref ! ZoneActor.Init(ZoneConfig(/*minW, maxW, minH, maxH,*/ c))
    }

    refs.head._2 ! ZoneActor.AddFood(100, 100)
