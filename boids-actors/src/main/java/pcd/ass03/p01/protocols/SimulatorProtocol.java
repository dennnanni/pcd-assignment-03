package pcd.ass03.p01.protocols;

import akka.actor.typed.ActorRef;
import pcd.ass03.p01.P2d;

import java.util.List;

public interface SimulatorProtocol {
	record Initialization(ActorRef<ModelProtocol> model, ActorRef<ViewProtocol> view) implements SimulatorProtocol {}

	record Start(int boidsCount, double alignment, double separation, double cohesion) implements SimulatorProtocol {}

	record Stop() implements SimulatorProtocol {}

	record Pause() implements SimulatorProtocol {}

	record Resume() implements SimulatorProtocol {}

	record UpdateParameters(double alignment, double separation, double cohesion) implements SimulatorProtocol {}

	record Tick() implements SimulatorProtocol {}

	record Update(List<P2d> positions) implements SimulatorProtocol {}
}
