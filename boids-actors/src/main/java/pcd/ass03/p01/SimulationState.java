package pcd.ass03.p01;

public class SimulationState {

    private boolean stopped;
    private boolean paused;

    public SimulationState() {
        this.stopped = true;
        this.paused = false;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        if (!stopped) {
            this.paused = false;
        }
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public void start() {
        this.stopped = false;
        this.paused = false;
    }

    public void stop() {
        this.stopped = true;
        this.paused = true;
    }
}
