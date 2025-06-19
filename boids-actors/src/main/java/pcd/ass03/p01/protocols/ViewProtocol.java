package pcd.ass03.p01.protocols;

import akka.actor.typed.ActorRef;
import pcd.ass03.p01.P2d;

import java.util.List;

public interface ViewProtocol {
	record Initialization(ActorRef<SimulatorProtocol> simulator) implements ViewProtocol {}

	record Update(int framerate, List<P2d> positions) implements ViewProtocol {}
}
