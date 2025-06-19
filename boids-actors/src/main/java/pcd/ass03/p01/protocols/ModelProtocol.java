package pcd.ass03.p01.protocols;

import akka.actor.typed.ActorRef;

public interface ModelProtocol {
	record Initialization(ActorRef<SimulatorProtocol> simulator) implements ModelProtocol {}

	record Setup(CommonData.InitParameters initParameters) implements ModelProtocol {}

	record Stop() implements ModelProtocol {}

	record UpdateParameters(CommonData.Parameters parameters) implements ModelProtocol {}

	record ComputeNewPositions() implements ModelProtocol {}
}
