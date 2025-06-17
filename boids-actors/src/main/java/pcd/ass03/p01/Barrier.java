package pcd.ass03.p01;

public class Barrier {

    private final int workersCount;
    private int counter;

    public Barrier(int workersCount) {
        this.workersCount = workersCount;
        this.counter = workersCount;
    }

    public synchronized void await() throws InterruptedException {
        counter--;
        if (counter > 0) {
            wait();
        } else {
            counter = workersCount;
            notifyAll();
        }
    }
}
