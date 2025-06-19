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

	public BoidsViewActor(ActorContext<ViewProtocol> context) {
		super(context);
	}

	public static Behavior<ViewProtocol> create() {
		return Behaviors.setup(BoidsViewActor::new);
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
		// TODO: create constants
		view = new BoidsView(600, 800);
		view.setStartSimulation(init -> simulator.tell(new SimulatorProtocol.Start(init)));
		view.setStopSimulation(() -> simulator.tell(new SimulatorProtocol.Stop()));
		view.setPauseSimulation(() -> simulator.tell(new SimulatorProtocol.Pause()));
		view.setResumeSimulation(() -> simulator.tell(new SimulatorProtocol.Resume()));
		view.setUpdateFactors(factors -> simulator.tell(new SimulatorProtocol.UpdateParameters(factors)));
		return this;
	}

	private Behavior<ViewProtocol> onUpdate(ViewProtocol.Update update) {
		getContext().getLog().info("View updated: framerate = {}, positions = {}", update.framerate(), update.positions().size());
		view.update(update.framerate(), update.positions());
		return this;
	}
}
