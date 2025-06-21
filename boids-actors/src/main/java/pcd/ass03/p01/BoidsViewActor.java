package pcd.ass03.p01;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.p01.protocols.SimulatorProtocol;
import pcd.ass03.p01.protocols.ViewProtocol;

public class BoidsViewActor extends AbstractBehavior<ViewProtocol> {

	private ActorRef<SimulatorProtocol> simulator;
	private BoidsView view;
	private final int width;
	private final int height;

	public BoidsViewActor(ActorContext<ViewProtocol> context, int width, int height) {
		super(context);
		this.width = width;
		this.height = height;
	}

	public static Behavior<ViewProtocol> create(int width, int height) {
		return Behaviors.setup(ctx -> new BoidsViewActor(ctx, width, height));
	}

	@Override
	public Receive<ViewProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(ViewProtocol.Initialization.class, this::onInitialization)
				.onMessage(ViewProtocol.Update.class, this::onUpdate)
				.build();
	}

	private Behavior<ViewProtocol> onInitialization(ViewProtocol.Initialization initialization) {
		getContext().getLog().info("View initialized");
		simulator = initialization.simulator();
		view = new BoidsView(width, height);
		view.setStartSimulation(init -> simulator.tell(new SimulatorProtocol.Start(
				init.boidsCount(), init.alignment(), init.separation(), init.cohesion())));
		view.setStopSimulation(() -> simulator.tell(new SimulatorProtocol.Stop()));
		view.setPauseSimulation(() -> simulator.tell(new SimulatorProtocol.Pause()));
		view.setResumeSimulation(() -> simulator.tell(new SimulatorProtocol.Resume()));
		view.setUpdateParameters(par -> simulator.tell(new SimulatorProtocol.UpdateParameters(
				par.alignment(), par.separation(), par.cohesion())));
		return this;
	}

	private Behavior<ViewProtocol> onUpdate(ViewProtocol.Update update) {
		view.update(update.framerate(), update.positions());
		return this;
	}
}
