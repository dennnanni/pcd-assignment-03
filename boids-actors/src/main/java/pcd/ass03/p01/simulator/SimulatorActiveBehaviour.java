package pcd.ass03.p01.simulator;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;
import pcd.ass03.p01.protocols.ViewProtocol;

import java.time.Duration;

public class SimulatorActiveBehaviour extends AbstractBehavior<SimulatorProtocol> {

	private static final int FRAMERATE = 25; // frames per second
	private SimulatorState state;
	private long time;
	private int framerate = 25;

	public SimulatorActiveBehaviour(ActorContext<SimulatorProtocol> context, SimulatorState state) {
		super(context);
		this.state = state;
	}

	public static Behavior<SimulatorProtocol> create(SimulatorState state) {
		return Behaviors.setup(ctx -> new SimulatorActiveBehaviour(ctx, state));
	}

	public SimulatorActiveBehaviour withView(ActorRef<ViewProtocol> view) {
		this.state = new SimulatorState(state.getModel(), view);
		return this;
	}

	@Override
	public Receive<SimulatorProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(SimulatorProtocol.Initialization.class, this::onInitialization)
				.onMessage(SimulatorProtocol.Start.class, this::onStart)
				.onMessage(SimulatorProtocol.Stop.class, this::onStop)
				.onMessage(SimulatorProtocol.Pause.class, this::onPause)
				.onMessage(SimulatorProtocol.UpdateParameters.class, this::onParametersChange)
				.onMessage(SimulatorProtocol.Update.class, this::onUpdateBoids)
				.onMessage(SimulatorProtocol.Tick.class, this::onTick)
				.build();
	}

	private Behavior<SimulatorProtocol> onTick(SimulatorProtocol.Tick tick) {
		this.time = System.currentTimeMillis();
		return this;
	}

	private Behavior<SimulatorProtocol> onInitialization(SimulatorProtocol.Initialization initialization) {
		getContext().getLog().info("Simulator initialized");
		this.state = new SimulatorState(initialization.model(), initialization.view());
		return this;
	}

	private Behavior<SimulatorProtocol> onStart(SimulatorProtocol.Start start) {
		getContext().getLog().info("Starting simulation");
		state.getModel().tell(new ModelProtocol.Setup(start.boidsCount(), start.alignment(), start.separation(), start.cohesion()));
		this.time = System.currentTimeMillis();
		return this;
	}

	private Behavior<SimulatorProtocol> onStop(SimulatorProtocol.Stop stop) {
		getContext().getLog().info("Stopping simulation");
		state.getModel().tell(new ModelProtocol.Stop());
		return this;
	}

	private Behavior<SimulatorProtocol> onPause(SimulatorProtocol.Pause pause) {
		getContext().getLog().info("Pausing simulation");
		return SimulatorPausedBehaviour.create(state);
	}

	private Behavior<SimulatorProtocol> onParametersChange(SimulatorProtocol.UpdateParameters update) {
		getContext().getLog().info("Updating simulation parameters");
		state.getModel().tell(new ModelProtocol.UpdateParameters(update.alignment(), update.separation(), update.cohesion()));
		return this;
	}

	private Behavior<SimulatorProtocol> onUpdateBoids(SimulatorProtocol.Update update) {
		long elapsedTime = System.currentTimeMillis() - this.time;
		long frameratePeriod = 1000 / FRAMERATE;

		if (elapsedTime < frameratePeriod) {
			Duration delay = Duration.ofMillis(frameratePeriod - elapsedTime);

			framerate = FRAMERATE;
			getContext().scheduleOnce(delay, state.getView(), new ViewProtocol.Update(framerate, update.positions()));
			getContext().scheduleOnce(delay, state.getModel(), new ModelProtocol.ComputeNewPositions());
			getContext().scheduleOnce(delay, getContext().getSelf(), new SimulatorProtocol.Tick());
		} else {
			framerate = (int) (1000 / elapsedTime);
			state.getView().tell(new ViewProtocol.Update(framerate, update.positions()));
			state.getModel().tell(new ModelProtocol.ComputeNewPositions());
			getContext().getSelf().tell(new SimulatorProtocol.Tick());
		}

		return this;
	}

}
