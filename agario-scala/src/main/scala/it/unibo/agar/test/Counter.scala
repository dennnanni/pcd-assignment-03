package it.unibo.agar.test

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}


private sealed trait CounterEvent
private final case class Initialized() extends CounterEvent
private final case class Incremented() extends CounterEvent
private final case class Decremented() extends CounterEvent

final case class CounterState(initialized: Boolean = false, count: Int = -1) {
  def applyEvent(event: CounterEvent): CounterState = event match {
    case Initialized() => copy(initialized = true, count = 0)
    case Incremented() => copy(count = count + 1)
    case Decremented() => copy(count = count - 1)
  }
}

object CounterState {
  val empty: CounterState = CounterState()
}

object CounterActor:
  sealed trait Command
  final case class Init() extends Command
  final case class Increment() extends Command
  final case class Decrement() extends Command

  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("CounterActor")
  
  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      EventSourcedBehavior[Command, CounterEvent, CounterState](
        persistenceId = PersistenceId.ofUniqueId(context.self.path.name),
        emptyState = CounterState.empty,
        commandHandler = (state, cmd) => cmd match {
          case Init() => 
            if !state.initialized then 
              context.log.info("Initializing counter")
              Effect.persist(Initialized())
            else
              context.log.warn("Counter already initialized, ignoring Init command")
              Effect.none
          case Increment() => 
            if state.initialized then { 
              context.log.info(s"Incrementing counter ${state.count}")
              Effect.persist(Incremented())
            } else {
            context.log.warn("Counter not initialized, ignoring Increment command")
            Effect.none
          }
          case Decrement() => if state.initialized then Effect.persist(Decremented()) else {
            context.log.warn("Counter not initialized, ignoring Decrement command")
            Effect.none
          }
        },
        eventHandler = (state, event) => state.applyEvent(event)
      )
    }