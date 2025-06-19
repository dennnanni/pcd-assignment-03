package pcd.ass03.p01.protocols;

import akka.actor.typed.ActorRef;

public interface SimulatorProtocol {
	record Initialization(ActorRef<ModelProtocol> model, ActorRef<ViewProtocol> view) implements SimulatorProtocol {}

	record Start(CommonData.InitParameters initParameters) implements SimulatorProtocol {}

	record Stop() implements SimulatorProtocol {}

	record Pause() implements SimulatorProtocol {}

	record Resume() implements SimulatorProtocol {}

	record UpdateFactors(CommonData.Factors factors) implements SimulatorProtocol {}
}
