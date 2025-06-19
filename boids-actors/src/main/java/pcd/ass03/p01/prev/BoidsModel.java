package pcd.ass03.p01.prev;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.SimulatorProtocol;

import java.util.ArrayList;
import java.util.List;

public class BoidsModel {

    private ActorRef<SimulatorProtocol> controller;
    
    private List<Boid> boids;
    private List<Boid> boidsCopy;
    private double separationWeight;
    private double alignmentWeight; 
    private double cohesionWeight; 
    private final double width;
    private final double height;
    private final double maxSpeed;
    private final double perceptionRadius;
    private final double avoidRadius;

    public BoidsModel(
            double initialSeparationWeight,
            double initialAlignmentWeight,
            double initialCohesionWeight,
            double width,
            double height,
            double maxSpeed,
            double perceptionRadius,
            double avoidRadius){
        separationWeight = initialSeparationWeight;
        alignmentWeight = initialAlignmentWeight;
        cohesionWeight = initialCohesionWeight;
        this.width = width;
        this.height = height;
        this.maxSpeed = maxSpeed;
        this.perceptionRadius = perceptionRadius;
        this.avoidRadius = avoidRadius;
    }

    public void createBoids(int count) {
        boids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            P2d pos = new P2d(-width/2 + Math.random() * width, -height/2 + Math.random() * height);
            V2d vel = new V2d(Math.random() * maxSpeed/2 - maxSpeed/4, Math.random() * maxSpeed/2 - maxSpeed/4);
            boids.add(new Boid(pos, vel));
        }

        makeCopy();
    }
    
    public List<Boid> getBoids(){
    	return boids;
    }
    
    public double getMinX() {
    	return -width/2;
    }

    public double getMaxX() {
    	return width/2;
    }

    public double getMinY() {
    	return -height/2;
    }

    public double getMaxY() {
    	return height/2;
    }
    
    public double getWidth() {
    	return width;
    }
 
    public double getHeight() {
    	return height;
    }

    public synchronized void setSeparationWeight(double value) {
    	this.separationWeight = value;
    }

    public synchronized void setAlignmentWeight(double value) {
    	this.alignmentWeight = value;
    }

    public synchronized void setCohesionWeight(double value) {
    	this.cohesionWeight = value;
    }

    public synchronized double getSeparationWeight() {
    	return separationWeight;
    }

    public synchronized double getCohesionWeight() {
    	return cohesionWeight;
    }

    public synchronized double getAlignmentWeight() {
    	return alignmentWeight;
    }
    
    public double getMaxSpeed() {
    	return maxSpeed;
    }

    public double getAvoidRadius() {
    	return avoidRadius;
    }

    public double getPerceptionRadius() {
    	return perceptionRadius;
    }

    public void makeCopy() {
        List<Boid> copy = new ArrayList<>();
        for (Boid b : boids) {
            copy.add(b.clone());
        }
        boidsCopy = copy;
    }

    public List<Boid> getBoidsCopy() {
        return boidsCopy;
    }

    public static Behavior<ModelProtocol> create() {
        return behavior(null);
    }

    public static Behavior<ModelProtocol> behavior(ActorRef<SimulatorProtocol> controller) {
        return Behaviors.receive(ModelProtocol.class)
            .onMessage(ModelProtocol.Initialization.class, message -> behavior(message.simulator()))
            .build();
    }
}
