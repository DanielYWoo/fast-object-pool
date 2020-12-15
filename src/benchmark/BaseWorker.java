public abstract class BaseWorker extends Thread {

    private final Benchmark benchmark;
    private final int id;
    private final long loop;
    protected final int borrowsPerLoop;
    long err = 0;

    public BaseWorker(Benchmark benchmark, int id, int borrowsPerLoop, long loop) {
        this.benchmark = benchmark;
        this.id = id;
        this.loop = loop;
        this.borrowsPerLoop = borrowsPerLoop;
    }

    @Override public void run() {
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            doSomething();
        }
        long t2 = System.currentTimeMillis();
        benchmark.latch.countDown();
        synchronized (benchmark) {
            benchmark.statsAvgRespTime[id] =  ((double) (t2 - t1)) / loop;
            benchmark.statsErrCount[id] = err;
        }
    }

    public abstract void doSomething();

}
