package pcd.ass03.p01.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.DispatcherSelector;
import pcd.ass03.p01.Boid;
import pcd.ass03.p01.BoidActor;
import pcd.ass03.p01.P2d;
import pcd.ass03.p01.V2d;
import pcd.ass03.p01.protocols.BoidsProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelState {

	private ActorRef<SimulatorProtocol> simulator;
	private Map<Integer, Boid> boids = new HashMap<>();
	private int boidsCount;
	private int width;
	private int height;
	private double maxSpeed = 4.0; // Default max speed
	private double perceptionRadius = 50.0; // Default perception radius
	private double avoidRadius = 20.0; // Default avoid radius
	private double alignmentWeight;
	private double separationWeight;
	private double cohesionWeight;

	public ModelState(ActorRef<SimulatorProtocol> simulator, int width, int height) {
		this.simulator = simulator;
		this.width = width;
		this.height = height;
	}

	public ModelState withSimulator(ActorRef<SimulatorProtocol> simulator) {
		return new ModelState(simulator, width, height);
	}

	public double getAlignmentWeight() {
		return alignmentWeight;
	}

	public void setAlignmentWeight(double alignmentWeight) {
		this.alignmentWeight = alignmentWeight;
	}

	public double getSeparationWeight() {
		return separationWeight;
	}

	public void setSeparationWeight(double separationWeight) {
		this.separationWeight = separationWeight;
	}

	public double getCohesionWeight() {
		return cohesionWeight;
	}

	public void setCohesionWeight(double cohesionWeight) {
		this.cohesionWeight = cohesionWeight;
	}

	public int getBoidsCount() {
		return boidsCount;
	}

	public void setBoidsCount(int boidsCount) {
		this.boidsCount = boidsCount;
	}

	public ActorRef<SimulatorProtocol> getSimulator() {
		return simulator;
	}

	public void setSimulator(ActorRef<SimulatorProtocol> simulator) {
		this.simulator = simulator;
	}

	public List<Boid> getBoids() {
		return boids.values().stream().toList();
	}

	public List<P2d> getBoidsPositions() {
		return boids.values().stream().map(Boid::getPos).toList();
	}

	public void clearBoids() {
		boids.clear();
	}

	public Boid spawnBoid(int id) {
		P2d pos = new P2d(-width/2 + Math.random() * width, -height/2 + Math.random() * height);
		V2d vel = new V2d(Math.random() * maxSpeed/2 - maxSpeed/4, Math.random() * maxSpeed/2 - maxSpeed/4);

		Boid boid = new Boid(id, pos, vel, avoidRadius, maxSpeed, width, height);
		boids.put(id, boid);
		return boid;
	}

	public void updateBoid(int id, Boid boid) {
		boids.put(id, boid.clone());
	}

	public List<Boid> getNearbyBoids(Boid boid) {
		var list = new ArrayList<Boid>();
		for (Boid other : boids.values()) {
			if (!other.equals(boid)) {
				double distance = boid.getPos().distance(other.getPos());
				if (distance < perceptionRadius) {
					list.add(other);
				}
			}
		}
		return list;
	}
}
