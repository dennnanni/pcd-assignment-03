package pcd.ass01;

import java.util.Optional;

public class BoidsSimulator {

    private final SimulationStateMonitor monitor;
    private BoidsModel model;
    private Optional<BoidsView> view;
    
    private static final int FRAMERATE = 25;
    private int framerate;
    
    public BoidsSimulator(BoidsModel model, SimulationStateMonitor monitor) {
        this.model = model;
        view = Optional.empty();
        this.monitor = monitor;
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }
      
    public void runSimulation() {
    	while (true) {
            this.monitor.waitIfPaused();
            var t0 = System.currentTimeMillis();
    		var boids = model.getBoids();

    		for (Boid boid : boids) {
                boid.updateVelocity(model);
            }

    		for (Boid boid : boids) {
                boid.updatePos(model);
            }

    		if (view.isPresent()) {
            	view.get().update(framerate);
            	var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var frameratePeriod = 1000/FRAMERATE;
                
                if (dtElapsed < frameratePeriod) {
                	try {
                		Thread.sleep(frameratePeriod - dtElapsed);
                	} catch (Exception ex) {}
                	framerate = FRAMERATE;
                } else {
                	framerate = (int) (1000/dtElapsed);
                }
    		}
            
    	}
    }
}
