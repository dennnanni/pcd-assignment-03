package pcd.ass03.p01.prev;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;
import pcd.ass03.p01.protocols.ViewProtocol;

public class BoidsSimulation {

	final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1000; 
	final static int ENVIRONMENT_HEIGHT = 1000;
    static final double MAX_SPEED = 4.0;
    static final double PERCEPTION_RADIUS = 50.0;
    static final double AVOID_RADIUS = 20.0;

	final static int SCREEN_WIDTH = 800; 
	final static int SCREEN_HEIGHT = 800;
	public static final int N_THREADS = Runtime.getRuntime().availableProcessors() + 1;


	public static void main(String[] args) {

//        SimulationStateMonitor stateMonitor = new SimulationStateMonitor();
//        SyncWorkersMonitor syncMonitor = new SyncWorkersMonitor(N_THREADS);
//
//		var model = new BoidsModel(
//				SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
//				ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
//				MAX_SPEED,
//				PERCEPTION_RADIUS,
//				AVOID_RADIUS);
//		var sim = new BoidsSimulator(model, stateMonitor, syncMonitor);
//		var view = new BoidsView(model, stateMonitor, SCREEN_WIDTH, SCREEN_HEIGHT);
//		sim.attachView(view);
		//sim.runSimulation();

		// TODO: initialize parameters of the actor
		Behavior<Void> rootBehavior = Behaviors.setup(context -> {
			ActorRef<SimulatorProtocol> simulatorActor =
					context.spawn(BoidsSimulator.create(), "boids-simulator");
			ActorRef<ModelProtocol> modelActor =
					context.spawn(BoidsModel.create(), "boids-model");
			ActorRef<ViewProtocol> viewActor =
					context.spawn(BoidsView.create(), "boids-view");

			// invio dei messaggi di inizializzazione
			viewActor.tell(new ViewProtocol.Initialization(simulatorActor));
			modelActor.tell(new ModelProtocol.Initialization(simulatorActor));
			simulatorActor.tell(new SimulatorProtocol.Initialization(modelActor, viewActor));

			return Behaviors.empty();
		});

		ActorSystem<Void> system = ActorSystem.create(rootBehavior, "boids-simulation");
    }
}
