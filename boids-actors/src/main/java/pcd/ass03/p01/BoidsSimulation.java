package pcd.ass03.p01;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import pcd.ass03.p01.model.ModelSetupBehaviour;
import pcd.ass03.p01.model.ModelState;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;
import pcd.ass03.p01.protocols.ViewProtocol;
import pcd.ass03.p01.simulator.SimulatorState;

public class BoidsSimulation {

	private static final int WIDTH = 600;
	private static final int HEIGHT = 800;

	public static void main(String[] args) {
		Behavior<Void> rootBehavior = Behaviors.setup(context -> {
			ActorRef<SimulatorProtocol> simulatorActor =
					context.spawn(BoidsSimulatorActor.create(new SimulatorState()), "boids-simulator");
//			ActorRef<ModelProtocol> modelActor =
//					context.spawn(BoidsModelActor.create(WIDTH, HEIGHT), "boids-model");
			ActorRef<ModelProtocol> modelActor =
					context.spawn(ModelSetupBehaviour.create(new ModelState(null, WIDTH, HEIGHT)), "boids-model");
			ActorRef<ViewProtocol> viewActor =
					context.spawn(BoidsViewActor.create(WIDTH, HEIGHT), "boids-view");

			// invio dei messaggi di inizializzazione
			viewActor.tell(new ViewProtocol.Initialization(simulatorActor));
			modelActor.tell(new ModelProtocol.Initialization(simulatorActor));
			simulatorActor.tell(new SimulatorProtocol.Initialization(modelActor, viewActor));

			return Behaviors.empty();
		});

		ActorSystem<Void> system = ActorSystem.create(rootBehavior, "boids-simulation");
	}
}
