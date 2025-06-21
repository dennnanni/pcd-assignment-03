package pcd.ass03.p01.model;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.p01.protocols.ModelProtocol;

public class ModelSetupBehaviour extends AbstractBehavior<ModelProtocol> {

	private final ModelState state;

	public ModelSetupBehaviour(ActorContext<ModelProtocol> context, ModelState state) {
		super(context);
		this.state = state;
	}

	public static Behavior<ModelProtocol> create(ModelState state) {
		return Behaviors.setup(ctx -> new ModelSetupBehaviour(ctx, state));
	}

	@Override
	public Receive<ModelProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(ModelProtocol.Initialization.class, this::onInitialization)
				.onMessage(ModelProtocol.Setup.class, this::onSetup)
				.build();
	}

	private Behavior<ModelProtocol> onInitialization(ModelProtocol.Initialization initialization) {
		return ModelSetupBehaviour.create(state.withSimulator(initialization.simulator()));
	}

	private Behavior<ModelProtocol> onSetup(ModelProtocol.Setup setup) {
		state.setBoidsCount(setup.boidsCount());
		state.setAlignmentWeight(setup.alignment());
		state.setSeparationWeight(setup.separation());
		state.setCohesionWeight(setup.cohesion());

		return ModelActiveBehaviour.create(state);
	}
}
