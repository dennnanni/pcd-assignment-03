package pcd.ass03.p01;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;

public class BoidsModelActor extends AbstractBehavior<ModelProtocol> {

	private ActorRef<SimulatorProtocol> simulator;

	public BoidsModelActor(ActorContext<ModelProtocol> context) {
		super(context);
	}

	public static Behavior<ModelProtocol> create() {
		return Behaviors.setup(BoidsModelActor::new);
	}

	@Override
	public Receive<ModelProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(ModelProtocol.Initialization.class, this::onInitialization)
				.build();
	}

	private Behavior<ModelProtocol> onInitialization(ModelProtocol.Initialization message) {
		getContext().getLog().info("Model initialized");
		this.simulator = message.simulator();
		return this;
	}
}
