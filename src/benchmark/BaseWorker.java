import java.util.concurrent.CountDownLatch;

public abstract class BaseWorker extends Thread {

    protected final Benchmark benchmark;
    protected final int id;
    protected final CountDownLatch latch;
    protected final long loop;
    protected long tb = 0;
    protected long tr = 0;

    public BaseWorker(Benchmark benchmark, int id, CountDownLatch latch, long loop) {
        this.benchmark = benchmark;
        this.id = id;
        this.latch = latch;
        this.loop = loop;
    }

    @Override public void run() {
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            doSomething();
        }
        long t2 = System.currentTimeMillis();
        latch.countDown();
        synchronized (benchmark) {
            benchmark.statsAvgRespTime[id] =  ((double) (t2 - t1)) / loop;
            benchmark.statsAvgBorrow[id] =  ((double) tb) / loop;
            benchmark.statsAvgReturn[id] =  ((double) tr) / loop;
        }
    }

    public abstract void doSomething();

}
