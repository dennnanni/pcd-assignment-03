package pcd.ass03.p01.protocols;

import akka.actor.typed.ActorRef;
import pcd.ass03.p01.Boid;

public interface ModelProtocol {
	record Initialization(ActorRef<SimulatorProtocol> simulator) implements ModelProtocol {}

	record Setup(int boidsCount, double alignment, double separation, double cohesion) implements ModelProtocol {}

	record Stop() implements ModelProtocol {}

	record UpdateParameters(double alignment, double separation, double cohesion) implements ModelProtocol {}

	record ComputeNewPositions() implements ModelProtocol {}

	record UpdateBoid(Boid boid) implements ModelProtocol {}
}
