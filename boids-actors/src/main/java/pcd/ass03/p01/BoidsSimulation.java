package pcd.ass03.p01;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;
import pcd.ass03.p01.protocols.ViewProtocol;

public class BoidsSimulation {

	public static void main(String[] args) {
		Behavior<Void> rootBehavior = Behaviors.setup(context -> {
			ActorRef<SimulatorProtocol> simulatorActor =
					context.spawn(BoidsSimulatorActor.create(), "boids-simulator");
			ActorRef<ModelProtocol> modelActor =
					context.spawn(BoidsModelActor.create(), "boids-model");
			ActorRef<ViewProtocol> viewActor =
					context.spawn(BoidsViewActor.create(), "boids-view");

			// invio dei messaggi di inizializzazione
			viewActor.tell(new ViewProtocol.Initialization(simulatorActor));
			modelActor.tell(new ModelProtocol.Initialization(simulatorActor));
			simulatorActor.tell(new SimulatorProtocol.Initialization(modelActor, viewActor));

			return Behaviors.empty();
		});

		ActorSystem<Void> system = ActorSystem.create(rootBehavior, "boids-simulation");
	}
}
