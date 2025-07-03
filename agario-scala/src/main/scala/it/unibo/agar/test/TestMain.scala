package it.unibo.agar.test

import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import it.unibo.agar.startup

object TestPersistence:
  def main(args: Array[String]): Unit =
    val behavior = EventSourcedBehavior[String, String, List[String]](
      persistenceId = PersistenceId.ofUniqueId("pippo"),
      emptyState = List.empty,
      commandHandler = (state, cmd) => Effect.persist(cmd),
      eventHandler = (state, evt) => state :+ evt
    )

    val ref = startup("agario", 25251) {
      behavior
    }

    ref ! "ciao"
    ref ! "mondo"


object TestShardingPersistence:
  def main(args: Array[String]): Unit =
    val ports = List(25251, 25252)
    val actorSystems = ports.map { port =>
      startup("testcassandra", port) {
        Behaviors.setup[Nothing] { context =>
          val sharding = ClusterSharding(context.system)

          sharding.init(Entity(CounterActor.TypeKey) { entityContext =>
            CounterActor()
          })
          Behaviors.empty
        }
      }
    }

    Thread.sleep(10000)
    println("ACTOR SYSTEMS: " + actorSystems.map(_.address))

    val counterActor1 = ClusterSharding(actorSystems(0)).entityRefFor(CounterActor.TypeKey, "counter1")
    val counterActor2 = ClusterSharding(actorSystems(1)).entityRefFor(CounterActor.TypeKey, "counter2")

    Thread.sleep(1000)
    println("Sending Init to counterActors")
    counterActor1 ! CounterActor.Init()
    counterActor2 ! CounterActor.Init()

    Thread.sleep(1000)

    val counter1From2 = ClusterSharding(actorSystems(1)).entityRefFor(CounterActor.TypeKey, "counter1")

    counter1From2 ! CounterActor.Increment()
    counter1From2 ! CounterActor.Increment()
    counterActor2 ! CounterActor.Increment()

    Thread.sleep(1000)

    actorSystems(0).terminate()

    val counter1From2Terminated = ClusterSharding(actorSystems(1)).entityRefFor(CounterActor.TypeKey, "counter1")

    Thread.sleep(1000)

    counter1From2Terminated ! CounterActor.Increment()



