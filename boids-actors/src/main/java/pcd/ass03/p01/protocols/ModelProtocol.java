package pcd.ass03.p01.protocols;

import akka.actor.typed.ActorRef;

public interface ModelProtocol {
	record Initialization(ActorRef<SimulatorProtocol> controller) implements ModelProtocol {}
}
