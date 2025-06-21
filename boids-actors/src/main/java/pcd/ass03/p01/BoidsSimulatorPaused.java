package pcd.ass03.p01;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import pcd.ass03.p01.protocols.SimulatorProtocol;
import pcd.ass03.p01.simulator.SimulatorState;

public class BoidsSimulatorPaused extends AbstractBehavior<SimulatorProtocol> {

	private final SimulatorState state;
	private final StashBuffer<SimulatorProtocol> buffer;

	public BoidsSimulatorPaused(ActorContext<SimulatorProtocol> context, SimulatorState state, StashBuffer<SimulatorProtocol> buffer) {
		super(context);
		this.state = state;
		this.buffer = buffer;
	}

	public static Behavior<SimulatorProtocol> create(SimulatorState state) {
		return  Behaviors.withStash(100, stash ->
				Behaviors.setup(ctx -> new BoidsSimulatorPaused(ctx, state, stash))
		);
	}

	@Override
	public Receive<SimulatorProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(SimulatorProtocol.Resume.class, this::onResume)
				.onMessage(SimulatorProtocol.Update.class, this::onUpdate)
				.build();
	}

	private Behavior<SimulatorProtocol> onResume(SimulatorProtocol.Resume resume) {
		return Behaviors.setup(ctx ->
			buffer.unstashAll(new BoidsSimulatorActor(ctx, state))
		);
	}

	private Behavior<SimulatorProtocol> onUpdate(SimulatorProtocol.Update update) {
		buffer.stash(update);
		return this;
	}
}
