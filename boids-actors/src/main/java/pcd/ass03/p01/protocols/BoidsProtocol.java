package pcd.ass03.p01.protocols;

import akka.actor.typed.ActorRef;
import pcd.ass03.p01.Boid;
import pcd.ass03.p01.P2d;
import pcd.ass03.p01.V2d;

import java.util.List;

public interface BoidsProtocol {
	record Setup(Boid boid) implements BoidsProtocol {}

	record ComputeNewPosition(List<Boid> nearby,
							  double alignmentWeight,
							  double separationWeight,
							  double cohesionWeight,
							  ActorRef<ModelProtocol> replyTo) implements BoidsProtocol {}
}
