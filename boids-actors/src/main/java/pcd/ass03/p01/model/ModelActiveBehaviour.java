package pcd.ass03.p01.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import pcd.ass03.p01.Boid;
import pcd.ass03.p01.BoidActor;
import pcd.ass03.p01.protocols.BoidsProtocol;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class ModelActiveBehaviour extends AbstractBehavior<ModelProtocol> {

	private final ModelState state;
	private List<ActorRef<BoidsProtocol>> boidsActors = new ArrayList<>();
	private ExecutorService customExecutor = Executors.newFixedThreadPool(16);
	private int updateCount = 0;

	public ModelActiveBehaviour(ActorContext<ModelProtocol> context, ModelState state) {
		super(context);
		this.state = state;

		spawnBoids(state.getBoidsCount());
		getContext().getSelf().tell(new ModelProtocol.ComputeNewPositions());
	}

	public static Behavior<ModelProtocol> create(ModelState state) {
		return Behaviors.setup(ctx -> new ModelActiveBehaviour(ctx, state));
	}

	@Override
	public Receive<ModelProtocol> createReceive() {
		return newReceiveBuilder()
				.onMessage(ModelProtocol.UpdateParameters.class, this::onUpdateParameters)
				.onMessage(ModelProtocol.ComputeNewPositions.class, this::computeNewPositions)
				.onMessage(ModelProtocol.UpdateBoid.class, this::updateBoid)
				.onMessage(ModelProtocol.Stop.class, this::onStop)
				.build();
	}

	private Behavior<ModelProtocol> computeNewPositions(ModelProtocol.ComputeNewPositions computeNewPositions) {
		computeAndSend(id -> boidsActors.get(id), customExecutor);
		return this;
	}

	private Behavior<ModelProtocol> updateBoid(ModelProtocol.UpdateBoid updateBoid) {
		Boid boid = updateBoid.boid();
		state.updateBoid(boid.getId(), boid);

		updateCount++;
		if (updateCount == state.getBoidsCount()) {
			state.getSimulator().tell(new SimulatorProtocol.Update(state.getBoidsPositions()));
			updateCount = 0;
		}

		return this;
	}

	private Behavior<ModelProtocol> onUpdateParameters(ModelProtocol.UpdateParameters updateParameters) {
		state.setAlignmentWeight(updateParameters.alignment());
		state.setSeparationWeight(updateParameters.separation());
		state.setCohesionWeight(updateParameters.cohesion());
		return this;
	}

	private Behavior<ModelProtocol> onStop(ModelProtocol.Stop stop) {
		terminateActors();
		return ModelSetupBehaviour.create(state);
	}

	public void computeAndSend(
			Function<Integer, ActorRef<BoidsProtocol>> chooseTarget,
			Executor executor
	) {
		for (Boid b : state.getBoids()) {
			CompletableFuture
				.supplyAsync(() -> state.getNearbyBoids(b), executor)
				.thenAcceptAsync(result -> {
					ActorRef<BoidsProtocol> target = chooseTarget.apply(b.getId());
					target.tell(new BoidsProtocol.ComputeNewPosition(
							result,
							state.getAlignmentWeight(),
							state.getSeparationWeight(),
							state.getCohesionWeight(),
							getContext().getSelf()));
				}, executor)
				.exceptionally(ex -> {
					getContext().getLog().error("Error computing new position for boid {}: {}", b.getId(), ex.getMessage());
					return null;
				});
		}
	}

	private void spawnBoids(int count) {
		for (int i = 0; i < count; i++) {
			ActorRef<BoidsProtocol> boidActor = getContext().spawn(
					BoidActor.create(),
					"boid-" + i,
					DispatcherSelector.fromConfig("my-dispatcher"));
			boidsActors.add(boidActor);
			Boid boid = state.spawnBoid(i);
			boidActor.tell(new BoidsProtocol.Setup(boid.clone()));
		}
	}

	private void terminateActors() {
		for (ActorRef<BoidsProtocol> boidActor : boidsActors) {
			getContext().stop(boidActor);
		}
		boidsActors.clear();
		state.clearBoids();
	}
}
