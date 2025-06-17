package pcd.ass03.p01;

public class Latch {

    private final int workersCount;
    private int counter;

    public Latch(int workersCount) {
        this.workersCount = workersCount;
        this.counter = workersCount;
    }

    public synchronized void countDown() {
        counter--;

        if (counter == 0) {
            notify();
        }
    }
    public synchronized void await() throws InterruptedException {
        if (counter > 0) {
            wait();
        }
    }
}
