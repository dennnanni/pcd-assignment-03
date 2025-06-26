package it.unibo.agar

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}

object ZoneActor:
  trait Command extends Message
  case class Move(x: Double, y: Double) extends Command
  case class SetBounds(min: Int, max: Int) extends Command
  case class Ping(playerId: String, position: Double) extends Command

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("ZoneActor")

  def apply(zoneId: String): Behavior[Command] = Behaviors.setup { context =>
    var boundMax = 0
    var boundMin = 0
    var position = (0.0, 0.0)
    Behaviors.receiveMessage {
      case Move(x, y) =>
        position = (x, y)
        context.log.info(s"Player $zoneId moved to $position")
        Behaviors.same
      case SetBounds(min, max) =>
        boundMin = min
        boundMax = max
        context.log.info("Set bounds for zone {}: min = {}, max = {}", zoneId, boundMin, boundMax)
        Behaviors.same
      case Ping(p, position) =>
        context.log.info(s"Player $p pinged zone $zoneId at position $position")
        Behaviors.same
    }
  }

object PlayerActor:
  trait Command extends Message
  case class Move(x: Double, y: Double) extends Command
  case class Spawn(x: Double) extends Command

  def apply(playerId: String): Behavior[Command] = Behaviors.setup { context =>
    var position = (0.0, 0.0)
    Behaviors.receiveMessage {
      case Move(x, y) =>
        position = (x, y)
        context.log.info(s"Player $playerId moved to $position")
        Behaviors.same
      case Spawn(x) =>
        val zoneId = (x / 100 + 1).toInt
        context.log.info(s"Context id ${context.system.name}, ${context.system}, $zoneId")
        val zoneActor = ClusterSharding(context.system).entityRefFor(ZoneActor.TypeKey, s"zone$zoneId")
        Thread.sleep(7000)
        zoneActor ! ZoneActor.Ping(playerId, x) // Assuming y is always 0 for spawning
        Behaviors.same
    }
  }



object Main:
  def main(args: Array[String]): Unit =
    val system1 = startup("agario", 25253) {
      Behaviors.setup[Nothing] { ctx =>
        val sharding = ClusterSharding(ctx.system)
        ctx.log.info(s"Starting sharding in system ${ctx.system}")
        sharding.init(Entity(ZoneActor.TypeKey)(ctx => ZoneActor(ctx.entityId)))
        Behaviors.empty
      }
    }

    Thread.sleep(1000)

    val system2 = startup("agario", 25254) {
      Behaviors.setup[Nothing] { ctx =>
        val sharding = ClusterSharding(ctx.system)
        ctx.log.info(s"Starting sharding in system ${ctx.system}")
        sharding.init(Entity(ZoneActor.TypeKey)(ctx => ZoneActor(ctx.entityId)))
        Behaviors.empty
      }
    }

    Thread.sleep(1000)

    val system3 = startup("agario", 25255) {
      Behaviors.setup[Nothing] { ctx =>
        val sharding = ClusterSharding(ctx.system)
        ctx.log.info(s"Starting sharding in system ${ctx.system}")
        sharding.init(Entity(ZoneActor.TypeKey)(ctx => ZoneActor(ctx.entityId)))
        Behaviors.empty
      }
    }

    Thread.sleep(10000)


    val zone1 = ClusterSharding(system1).entityRefFor(ZoneActor.TypeKey, "zone1")
    val zone2 = ClusterSharding(system2).entityRefFor(ZoneActor.TypeKey, "zone2")
    val zone3 = ClusterSharding(system1).entityRefFor(ZoneActor.TypeKey, "zone3")
    val zone4 = ClusterSharding(system3).entityRefFor(ZoneActor.TypeKey, "zone4")

    Thread.sleep(5000)

    zone1 ! ZoneActor.SetBounds(0, 100)
    zone2 ! ZoneActor.SetBounds(100, 200)
    zone3 ! ZoneActor.SetBounds(200, 300)
    zone4 ! ZoneActor.SetBounds(300, 400)

    val playerSystem = startup("agario", 25256)(PlayerActor("player1"))

    Thread.sleep(10000)

    playerSystem ! PlayerActor.Spawn(50) // Player in zone1
    playerSystem ! PlayerActor.Spawn(150) // Player in zone2
    playerSystem ! PlayerActor.Spawn(250) // Player in zone3


