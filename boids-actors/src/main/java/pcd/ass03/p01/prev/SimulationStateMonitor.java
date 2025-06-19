package pcd.ass03.p01.prev;

public class SimulationStateMonitor {

    private boolean stopped;
    private boolean paused;

    public SimulationStateMonitor() {
        this.stopped = true;
        this.paused = false;
    }

    public synchronized boolean isPaused() {
        return this.paused;
    }

    public synchronized void pause() {
        this.paused = true;
    }

    public synchronized void resume() {
        if (!stopped) {
            this.paused = false;
            notifyAll();
        }
    }

    public synchronized void waitIfPausedOrStopped() throws InterruptedException {
        while (this.paused || this.stopped) {
            wait();
        }
    }

    public synchronized boolean isStopped() {
        return this.stopped;
    }

    public synchronized void start() {
        this.stopped = false;
        this.paused = false;
        notifyAll();
    }

    public synchronized void stop() throws InterruptedException {
        this.stopped = true;
        this.paused = true;
    }
}
