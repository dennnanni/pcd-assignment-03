package pcd.ass03.p01;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.p01.protocols.BoidsProtocol;
import pcd.ass03.p01.protocols.ModelProtocol;

public class BoidActor extends AbstractBehavior<BoidsProtocol> {

	private Boid boid;

	public BoidActor(ActorContext<BoidsProtocol> context) {
		super(context);
	}

	public static Behavior<BoidsProtocol> create() {
		return Behaviors.setup(BoidActor::new);
	}

	@Override
	public Receive<BoidsProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(BoidsProtocol.Setup.class, this::onSetup)
				.onMessage(BoidsProtocol.ComputeNewPosition.class, this::onComputeNewPosition)
				.build();
	}

	private Behavior<BoidsProtocol> onComputeNewPosition(BoidsProtocol.ComputeNewPosition computeNewPosition) {
		//getContext().getLog().info("Computing new position for boid: {}", boid.getId());

		double alignmentWeight = computeNewPosition.alignmentWeight();
		double separationWeight = computeNewPosition.separationWeight();
		double cohesionWeight = computeNewPosition.cohesionWeight();

		boid.updateVelocity(computeNewPosition.nearby(), alignmentWeight, separationWeight, cohesionWeight);

		boid.updatePos();

		// Notify the model about the updated boid
		computeNewPosition.replyTo().tell(new ModelProtocol.UpdateBoid(boid));
		//getContext().getLog().info("Sent {}", boid.getId());
		return this;
	}

	private Behavior<BoidsProtocol> onSetup(BoidsProtocol.Setup setup) {
		getContext().getLog().info("Boid setup with initial position: {}", setup.boid().getPos());

		this.boid = setup.boid().clone();

		return this;
	}
}
