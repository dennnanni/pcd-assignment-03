package pcd.ass03.p01.protocols;

import akka.actor.typed.ActorRef;

public interface ViewProtocol {
	record Initialization(ActorRef<SimulatorProtocol> simulator) implements ViewProtocol {}

}
