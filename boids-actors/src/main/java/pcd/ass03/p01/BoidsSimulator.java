package pcd.ass03.p01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator {

    private final int N_THREADS = Runtime.getRuntime().availableProcessors() + 1;
    private final SimulationStateMonitor stateMonitor;
    private final SyncWorkersMonitor workersMonitor;
    private final BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 25;
    private int framerate;
    private final List<Thread> workers;

    public BoidsSimulator(BoidsModel model, SimulationStateMonitor stateMonitor, SyncWorkersMonitor workersMonitor) {
        this.model = model;
        view = Optional.empty();
        this.stateMonitor = stateMonitor;
        this.workersMonitor = workersMonitor;
        this.workers = new ArrayList<>();
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }

    public void runSimulation() {
    	while (true) {
            try {
                this.stateMonitor.waitIfPausedOrStopped();
                if (workers.isEmpty()) {
                    createThreads();
                }
            } catch (InterruptedException ex) {}

            var t0 = System.currentTimeMillis();

            workersMonitor.waitWorkers();

            model.makeCopy();

    		if (view.isPresent()) {
            	view.get().update(framerate);
            	var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var framratePeriod = 1000/FRAMERATE;

                if (dtElapsed < framratePeriod) {
                	try {
                		Thread.sleep(framratePeriod - dtElapsed);
                	} catch (Exception ex) {}
                	framerate = FRAMERATE;
                } else {
                	framerate = (int) (1000/dtElapsed);
                }
    		}

            if (stateMonitor.isStopped()) {
                interruptThreads();
            }

            workersMonitor.coordinatorDone();
    	}
    }

    private void interruptThreads() {
        for (Thread t : workers) {
            t.interrupt();
        }
        workers.clear();
    }

    private void createThreads() {
        Barrier barrier = new Barrier(N_THREADS);
        int boids = model.getBoids().size();
		int divisionFactor = boids / N_THREADS + 1;
		for (int i = 0; i < boids; i += divisionFactor) {
			int controlledBoids = i + divisionFactor <= boids ? divisionFactor : (boids - i);
			Worker worker = new Worker(i, controlledBoids, model, stateMonitor, barrier, workersMonitor);
            workers.add(worker);
			worker.start();
		}
    }
}
