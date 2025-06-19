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

public class BoidsSimulatorActor extends AbstractBehavior<SimulatorProtocol> {

	private ActorRef<ModelProtocol> model;
	private ActorRef<ViewProtocol> view;

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
				.build();
	}

	private Behavior<SimulatorProtocol> onInitialization(SimulatorProtocol.Initialization initialization) {
		getContext().getLog().info("Simulator initialized");
		this.model = initialization.model();
		this.view = initialization.view();
		return this;
	}
}
