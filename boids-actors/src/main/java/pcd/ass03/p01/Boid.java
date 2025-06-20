package pcd.ass03.p01;

import java.util.List;

public class Boid {

	private final int id;
	private final double avoidRadius;
	private final double maxSpeed;
	private final double width;
	private final double height;
	private P2d pos;
    private V2d vel;


	public Boid(int id,
				P2d pos, V2d vel,
				double avoidRadius,
				double maxSpeed,
				double width,
				double height) {
		this.id = id;
    	this.pos = pos;
    	this.vel = vel;
		this.avoidRadius = avoidRadius;
		this.maxSpeed = maxSpeed;
		this.width = width;
		this.height = height;
    }

	public int getId() {
		return id;
	}

	public P2d getPos() {
    	return pos;
    }

    public V2d getVel() {
    	return vel;
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

    public void updateVelocity(List<Boid> nearby,
								double alignmentWeight,
								double separationWeight,
								double cohesionWeight) {

    	/* change velocity vector according to separation, alignment, cohesion */
    	
    	V2d separation = calculateSeparation(nearby);
    	V2d alignment = calculateAlignment(nearby);
    	V2d cohesion = calculateCohesion(nearby);
    	
    	vel = vel.sum(alignment.mul(alignmentWeight))
    			.sum(separation.mul(separationWeight))
    			.sum(cohesion.mul(cohesionWeight));
        
        /* Limit speed to MAX_SPEED */

        double speed = vel.abs();
        
        if (speed > maxSpeed) {
            vel = vel.getNormalized().mul(maxSpeed);
        }
    }    
    
    public void updatePos() {

        /* Update position */

        pos = pos.sum(vel);
        
        /* environment wrap-around */
        
        if (pos.x() < getMinX()) pos = pos.sum(new V2d(width, 0));
        if (pos.x() >= getMaxX()) pos = pos.sum(new V2d(-width, 0));
        if (pos.y() < getMinY()) pos = pos.sum(new V2d(0, height));
        if (pos.y() >= getMaxY()) pos = pos.sum(new V2d(0, -height));
    }     

    private V2d calculateAlignment(List<Boid> nearbyBoids) {
        double avgVx = 0;
        double avgVy = 0;
        if (!nearbyBoids.isEmpty()) {
	        for (Boid other : nearbyBoids) {
	        	V2d otherVel = other.getVel();
	            avgVx += otherVel.x();
	            avgVy += otherVel.y();
	        }	        
	        avgVx /= nearbyBoids.size();
	        avgVy /= nearbyBoids.size();
	        return new V2d(avgVx - vel.x(), avgVy - vel.y()).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }

    private V2d calculateCohesion(List<Boid> nearbyBoids) {
        double centerX = 0;
        double centerY = 0;
        if (!nearbyBoids.isEmpty()) {
	        for (Boid other: nearbyBoids) {
	        	P2d otherPos = other.getPos();
	            centerX += otherPos.x();
	            centerY += otherPos.y();
	        }
            centerX /= nearbyBoids.size();
            centerY /= nearbyBoids.size();
            return new V2d(centerX - pos.x(), centerY - pos.y()).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }
    
    private V2d calculateSeparation(List<Boid> nearbyBoids) {
        double dx = 0;
        double dy = 0;
        int count = 0;
        for (Boid other: nearbyBoids) {
        	P2d otherPos = other.getPos();
    	    double distance = pos.distance(otherPos);
    	    if (distance < avoidRadius) {
    	    	dx += pos.x() - otherPos.x();
    	    	dy += pos.y() - otherPos.y();
    	    	count++;
    	    }
    	}
        if (count > 0) {
            dx /= count;
            dy /= count;
            return new V2d(dx, dy).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }

	@Override
	public Boid clone() {
		return new Boid(id, new P2d(pos.x(), pos.y()), new V2d(vel.x(), vel.y()),
						avoidRadius, maxSpeed, width, height);
	}
}
