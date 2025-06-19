package pcd.ass03.p01.prev;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import pcd.ass03.p01.protocols.SimulatorProtocol;

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
                    // createActors();
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
                //interruptThreads();
            }

            workersMonitor.coordinatorDone();
    	}
    }

	public static Behavior<SimulatorProtocol> create() {
		return Behaviors.receive(SimulatorProtocol.class)
			.onMessage(SimulatorProtocol.Initialization.class, (pippo) -> {
				System.out.println("Simulator initialized");
				return Behaviors.same();
			})
			.build();
	}

}
