import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Daniel
 */
public class Benchmark {

    final String name;
    final double[] statsAvgRespTime;
    final long[] statsErrCount;
    final private int workerCount;
    final private int loop;
    final CountDownLatch latch;
    final AtomicLong created = new AtomicLong(0);
    final int borrowsPerLoop;
    BaseWorker[] workers;

    Benchmark(String name, int workerCount, int borrowsPerLoop, int loop) {
        this.name = name;
        this.workerCount = workerCount;
        this.loop = loop;
        this.latch = new CountDownLatch(workerCount);
        this.borrowsPerLoop = borrowsPerLoop;
        this.statsAvgRespTime = new double[workerCount];
        this.statsErrCount = new long[workerCount];
    }

    public BenchmarkResult testAndPrint() throws InterruptedException {
        System.out.println();
        System.out.println("Benchmark " + name + " with " + workerCount + " concurrent threads");
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < workerCount; i++) {
            workers[i].start();
        }
        latch.await();
        long t2 = System.currentTimeMillis();

        double stats = 0;
        for (int i = 0; i < workerCount; i++) {
            stats += statsAvgRespTime[i];
        }
        double latency = stats / workerCount;
        System.out.println("Average Response Time:" + new DecimalFormat("0.0000").format(latency));

        stats = 0;
        for (int i = 0; i < workerCount; i++) {
            stats += statsErrCount[i];
        }
        double errRate = stats * 100 / workerCount / loop;

        double throughput = (double) loop * workerCount / (t2 - t1);

        System.out.println("Error Ratio:" + new DecimalFormat("0.00%").format(errRate));
        System.out.println("Throughput Per Second:" + new DecimalFormat("0").format(throughput) + "K");
        System.out.println("Objects created:" + created.get());
        System.out.println();
        return new BenchmarkResult(name, workerCount, borrowsPerLoop, loop, created.get(), errRate, throughput, latency);
    }

}
