package pcd.ass03.p01;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.p01.protocols.BoidsProtocol;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class BoidsModelActor extends AbstractBehavior<ModelProtocol> {

	private ActorRef<SimulatorProtocol> simulator;
	private Map<Integer, Boid> boids = new HashMap<>();
	private List<ActorRef<BoidsProtocol>> boidsActors = new ArrayList<>();
	private int boidsCount;
	private int updateCount = 0;
	private int width;
	private int height;
	private double maxSpeed = 4.0; // Default max speed
	private double perceptionRadius = 50.0; // Default perception radius
	private double avoidRadius = 20.0; // Default avoid radius
	private double alignmentWeight;
	private double separationWeight;
	private double cohesionWeight;

	public BoidsModelActor(ActorContext<ModelProtocol> context, int width, int height) {
		super(context);
		this.width = width;
		this.height = height;
	}

	public static Behavior<ModelProtocol> create(int width, int height) {
		return Behaviors.setup(ctx -> new BoidsModelActor(ctx, width, height));
	}

	@Override
	public Receive<ModelProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(ModelProtocol.Initialization.class, this::onInitialization)
				.onMessage(ModelProtocol.Setup.class, this::setup)
				.onMessage(ModelProtocol.UpdateParameters.class, this::onUpdateParameters)
				.onMessage(ModelProtocol.ComputeNewPositions.class, this::computeNewPositions)
				.onMessage(ModelProtocol.UpdateBoid.class, this::updateBoid)
				.build();
	}

	private Behavior<ModelProtocol> onUpdateParameters(ModelProtocol.UpdateParameters updateParameters) {
		getContext().getLog().info("Updating model parameters");

		this.alignmentWeight = updateParameters.alignment();
		this.separationWeight = updateParameters.separation();
		this.cohesionWeight = updateParameters.cohesion();

		return this;
	}

	private Behavior<ModelProtocol> onInitialization(ModelProtocol.Initialization message) {
		getContext().getLog().info("Model initialized");
		this.simulator = message.simulator();
		return this;
	}

	private Behavior<ModelProtocol> setup(ModelProtocol.Setup setup) {
		getContext().getLog().info("Setting up the model with {} boids", setup.boidsCount());

		boidsCount = setup.boidsCount();
		alignmentWeight = setup.alignment();
		separationWeight = setup.separation();
		cohesionWeight = setup.cohesion();
		spawnBoids(boidsCount);

		getContext().scheduleOnce(Duration.ZERO, getContext().getSelf(), new ModelProtocol.ComputeNewPositions());
		return this;
	}

	private Behavior<ModelProtocol> computeNewPositions(ModelProtocol.ComputeNewPositions computeNewPositions) {
		//getContext().getLog().info("Computing new positions for boids");

		// TODO: Ci deve andare il pool di thread per le computazioni
		computeAndSend(id -> boidsActors.get(id), getContext().getExecutionContext());

		return this;
	}

	private Behavior<ModelProtocol> updateBoid(ModelProtocol.UpdateBoid updateBoid) {
		//getContext().getLog().info("Updating boid position and velocity");

		Boid boid = updateBoid.boid();
		boids.put(boid.getId(), boid.clone());

		updateCount++;
		if (updateCount == boidsCount) {
			getContext().getLog().info("All boids updated, sending new positions to simulator");
			simulator.tell(new SimulatorProtocol.Update(boids.values().stream().map(Boid::getPos).toList()));
			updateCount = 0; // Reset update count
		}

		return this;
	}

	private void spawnBoids(int count) {
//		Boid boid = new Boid(0, new P2d(1145483.0539733875, -176.76667012371175), new V2d(0, 0), avoidRadius, maxSpeed, width, height);
//		Boid boid2 = new Boid(1, new P2d(1115425.732347265, -150.70712268157527), new V2d(0, 0), avoidRadius, maxSpeed, width, height);
//		Boid boid3 = new Boid(2, new P2d(1052906.50336493, -96.50326400193053), new V2d(0, 0), avoidRadius, maxSpeed, width, height);
//		Boid boid4 = new Boid(3, new P2d(957925.3670264002, -14.15509408477715), new V2d(0, 0), avoidRadius, maxSpeed, width, height);
//		Boid boid5 = new Boid(3, new P2d(400, 400), new V2d(0, 0), avoidRadius, maxSpeed, width, height);
//
//		boids.put(0, boid);
//		boids.put(1, boid2);
//		boids.put(2, boid3);
//		boids.put(3, boid4);
//		boids.put(4, boid5);
//
//		ActorRef<BoidsProtocol> boidActor1 = getContext().spawn(BoidActor.create(), "boid-0");
//		ActorRef<BoidsProtocol> boidActor2 = getContext().spawn(BoidActor.create(), "boid-1");
//		ActorRef<BoidsProtocol> boidActor3 = getContext().spawn(BoidActor.create(), "boid-2");
//		ActorRef<BoidsProtocol> boidActor4 = getContext().spawn(BoidActor.create(), "boid-3");
//		ActorRef<BoidsProtocol> boidActor5 = getContext().spawn(BoidActor.create(), "boid-4");
//
//
//		boidActor1.tell(new BoidsProtocol.Setup(boid));
//		boidActor2.tell(new BoidsProtocol.Setup(boid4));
//		boidActor3.tell(new BoidsProtocol.Setup(boid2));
//		boidActor4.tell(new BoidsProtocol.Setup(boid3));
//		boidActor5.tell(new BoidsProtocol.Setup(boid5));

		for (int i = 0; i < count; i++) {
			P2d pos = new P2d(-width/2 + Math.random() * width, -height/2 + Math.random() * height);
			V2d vel = new V2d(Math.random() * maxSpeed/2 - maxSpeed/4, Math.random() * maxSpeed/2 - maxSpeed/4);

			getContext().getLog().info("Position {}, velocity {}", pos, vel);
			ActorRef<BoidsProtocol> boidActor = getContext().spawn(BoidActor.create(), "boid-" + i);
			boidsActors.add(boidActor);
			Boid boid = new Boid(
				i,
				pos,
				vel,
				avoidRadius,
				maxSpeed,
				width,
				height
			);
			boids.put(i, boid);
			boidActor.tell(new BoidsProtocol.Setup(boid.clone()));
		}
	}

	public void computeAndSend(
			Function<Integer, ActorRef<BoidsProtocol>> chooseTarget,
			Executor executor
	) {
		for (Entry<Integer, Boid> b : boids.entrySet()) {
			CompletableFuture
				.supplyAsync(() -> getNearbyBoids(b.getValue()), executor)
				.thenAcceptAsync(result -> {
					ActorRef<BoidsProtocol> target = chooseTarget.apply(b.getKey());
					target.tell(new BoidsProtocol.ComputeNewPosition(
							result, alignmentWeight, separationWeight, cohesionWeight, getContext().getSelf()));
				}, executor)
				.exceptionally(ex -> {
					getContext().getLog().error("Error computing new position for boid {}: {}", b.getKey(), ex.getMessage());
					return null;
				});
		}
	}

	private List<Boid> getNearbyBoids(Boid boid) {
    	var list = new ArrayList<Boid>();
        for (Boid other : boids.values()) {
        	if (!other.equals(boid)) {
        		double distance = boid.getPos().distance(other.getPos());
        		if (distance < perceptionRadius) {
        			list.add(other);
        		}
        	}
        }
        return list;
    }


}
