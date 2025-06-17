package pcd.ass01;

public class SimulationStateMonitor {

    private boolean paused;

    public SimulationStateMonitor(boolean paused) {
        this.paused = paused;
    }

    public synchronized boolean isPaused() {
        return this.paused;
    }

    public synchronized void pause() {
        this.paused = true;
    }

    public synchronized void resume() {
        this.paused = false;
        notifyAll();
    }

    public synchronized void waitIfPaused() {
        while (this.paused) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
    }
}
