package pcd.ass03.p01.protocols;

import akka.actor.typed.ActorRef;

public interface SimulatorProtocol {
	record Initialization(ActorRef<ModelProtocol> model, ActorRef<ViewProtocol> view) implements SimulatorProtocol {}

}
