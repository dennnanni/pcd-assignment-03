package pcd.ass03.p01;

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

public class BoidsSimulatorActor extends AbstractBehavior<SimulatorProtocol> {

	private static final int FRAMERATE = 25; // frames per second
	private ActorRef<ModelProtocol> model;
	private ActorRef<ViewProtocol> view;
	private long time;
	private int framerate = 25;

	public BoidsSimulatorActor(ActorContext<SimulatorProtocol> context) {
		super(context);
	}

	public static Behavior<SimulatorProtocol> create() {
		return Behaviors.setup(BoidsSimulatorActor::new);
	}

	@Override
	public Receive<SimulatorProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(SimulatorProtocol.Initialization.class, this::onInitialization)
				.onMessage(SimulatorProtocol.Start.class, this::onStart)
				.onMessage(SimulatorProtocol.Stop.class, this::onStop)
				.onMessage(SimulatorProtocol.Pause.class, this::onPause)
				.onMessage(SimulatorProtocol.Resume.class, this::onResume)
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
		this.model = initialization.model();
		this.view = initialization.view();
		return this;
	}

	private Behavior<SimulatorProtocol> onStart(SimulatorProtocol.Start start) {
		getContext().getLog().info("Starting simulation");
		model.tell(new ModelProtocol.Setup(start.boidsCount(), start.alignment(), start.separation(), start.cohesion()));
		this.time = System.currentTimeMillis();
		return this;
	}

	private Behavior<SimulatorProtocol> onStop(SimulatorProtocol.Stop stop) {
		getContext().getLog().info("Stopping simulation");
		model.tell(new ModelProtocol.Stop());
		return this;
	}

	private Behavior<SimulatorProtocol> onPause(SimulatorProtocol.Pause pause) {
		getContext().getLog().info("Pausing simulation");
		// TODO: when simulation is paused, timer is stopped and no messages should be handled
		return this;
	}

	private Behavior<SimulatorProtocol> onResume(SimulatorProtocol.Resume resume) {
		getContext().getLog().info("Resuming simulation");
		// TODO: start timer again
		return this;
	}

	private Behavior<SimulatorProtocol> onParametersChange(SimulatorProtocol.UpdateParameters update) {
		getContext().getLog().info("Updating simulation parameters");
		model.tell(new ModelProtocol.UpdateParameters(update.alignment(), update.separation(), update.cohesion()));
		return this;
	}

	private Behavior<SimulatorProtocol> onUpdateBoids(SimulatorProtocol.Update update) {
		long elapsedTime = System.currentTimeMillis() - this.time;
		long frameratePeriod = 1000 / FRAMERATE;

		if (elapsedTime < frameratePeriod) {
			Duration delay = Duration.ofMillis(frameratePeriod - elapsedTime);

			framerate = FRAMERATE;
			getContext().scheduleOnce(delay, view, new ViewProtocol.Update(framerate, update.positions()));
			getContext().scheduleOnce(delay, model, new ModelProtocol.ComputeNewPositions());
			getContext().scheduleOnce(delay, getContext().getSelf(), new SimulatorProtocol.Tick());
		} else {
			framerate = (int) (1000 / elapsedTime);
			view.tell(new ViewProtocol.Update(framerate, update.positions()));
			model.tell(new ModelProtocol.ComputeNewPositions());
			getContext().getSelf().tell(new SimulatorProtocol.Tick());
		}

		return this;
	}

}
