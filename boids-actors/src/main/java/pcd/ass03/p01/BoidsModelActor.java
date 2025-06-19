package pcd.ass03.p01;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoidsModelActor extends AbstractBehavior<ModelProtocol> {

	private ActorRef<SimulatorProtocol> simulator;
	private List<P2d> positions = new ArrayList<>();
	private int boidsCount;
	private int width = 300; // Default width
	private int height = 400; // Default height

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
				.onMessage(ModelProtocol.Setup.class, this::setup)
				.onMessage(ModelProtocol.ComputeNewPositions.class, this::computeNewPositions)
				.build();
	}

	private Behavior<ModelProtocol> computeNewPositions(ModelProtocol.ComputeNewPositions computeNewPositions) {
		getContext().getLog().info("Computing new positions for boids");
		positions.clear();
		Random random = new Random();
		for (int i = 0; i < boidsCount; i++) {
			positions.add(new P2d(random.nextInt(width), random.nextInt(height)));
		}
		simulator.tell(new SimulatorProtocol.Update(positions));
		return this;
	}

	private Behavior<ModelProtocol> setup(ModelProtocol.Setup setup) {
		getContext().getLog().info("Setting up the model with {} boids", setup.initParameters().boidsCount());
		boidsCount = setup.initParameters().boidsCount();
		getContext().scheduleOnce(Duration.ZERO, getContext().getSelf(), new ModelProtocol.ComputeNewPositions());
		return this;
	}

	private Behavior<ModelProtocol> onInitialization(ModelProtocol.Initialization message) {
		getContext().getLog().info("Model initialized");
		this.simulator = message.simulator();
		return this;
	}
}
