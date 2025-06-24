package pcd.ass03.p01.simulator;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;

public class SimulatorPausedBehaviour extends AbstractBehavior<SimulatorProtocol> {

	private final SimulatorState state;
	private final StashBuffer<SimulatorProtocol> buffer;

	public SimulatorPausedBehaviour(ActorContext<SimulatorProtocol> context, SimulatorState state, StashBuffer<SimulatorProtocol> buffer) {
		super(context);
		this.state = state;
		this.buffer = buffer;
	}

	public static Behavior<SimulatorProtocol> create(SimulatorState state) {
		return  Behaviors.withStash(100, stash ->
				Behaviors.setup(ctx -> new SimulatorPausedBehaviour(ctx, state, stash))
		);
	}

	@Override
	public Receive<SimulatorProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(SimulatorProtocol.Resume.class, this::onResume)
				.onMessage(SimulatorProtocol.Stop.class, this::onStop)
				.onMessage(SimulatorProtocol.Update.class, this::onUpdate)
				.onMessage(SimulatorProtocol.Tick.class, this::onTick)
				.build();
	}

	private Behavior<SimulatorProtocol> onStop(SimulatorProtocol.Stop stop) {
		state.getModel().tell(new ModelProtocol.Stop());
		return Behaviors.setup(ctx -> new SimulatorActiveBehaviour(ctx, state));
	}

	private Behavior<SimulatorProtocol> onResume(SimulatorProtocol.Resume resume) {
		return Behaviors.setup(ctx ->
			buffer.unstashAll(new SimulatorActiveBehaviour(ctx, state))
		);
	}

	private Behavior<SimulatorProtocol> onUpdate(SimulatorProtocol.Update update) {
		buffer.stash(update);
		return this;
	}

	private Behavior<SimulatorProtocol> onTick(SimulatorProtocol.Tick tick) {
		buffer.stash(tick);
		return this;
	}
}
