import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Daniel
 */
public class Benchmark {

    final double[] statsAvgRespTime;
    final long[] statsErrCount;
    final private int workerCount;
    final private int loop;
    final CountDownLatch latch;
    final AtomicLong created = new AtomicLong(0);
    BaseWorker[] workers;

    Benchmark(int workerCount, int loop) {
        this.workerCount = workerCount;
        this.loop = loop;
        this.latch = new CountDownLatch(workerCount);
        statsAvgRespTime = new double[workerCount];
        statsErrCount = new long[workerCount];
    }

    void testAndPrint() throws InterruptedException {
        System.out.println();
        System.out.println("Benchmark with " + workerCount + " concurrent threads");
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
        System.out.println("Average Response Time:" + new DecimalFormat("0.0000").format(stats / workerCount));

        stats = 0;
        for (int i = 0; i < workerCount; i++) {
            stats += statsErrCount[i];
        }
        System.out.println("Error Ratio:" + new DecimalFormat("0.00%").format(stats * 100 / workerCount / loop));

        System.out.println("Average Throughput Per Second:" + new DecimalFormat("0").format(((double) loop * workerCount) / (t2 - t1) ) + "k");
        System.out.println("Objects created:" + created.get());
        System.out.println();
    }

    public static void main(String[] args) throws Exception {

        System.out.println("-----------warm up------------");
        testFOP(50,  1000);
        testFOPDisruptor(50,  1000);
        testStormpot(50,  1000);
        testFurious(50,  1000);
        testCommon(50,  1000);

        System.out.println("-----------fast object pool------------");
        testFOP(50,  50000);
        testFOP(100, 50000);
        testFOP(150, 50000);
        testFOP(200, 30000);
        testFOP(250, 30000);
        testFOP(300, 30000);
        testFOP(350, 20000);
        testFOP(400, 20000);
        testFOP(450, 20000);
        testFOP(500, 10000);
        testFOP(550, 10000);
        testFOP(600, 10000);

        System.out.println("-----------fast object pool with disruptor------------");
        testFOPDisruptor(50,  50000);
        testFOPDisruptor(100, 50000);
        testFOPDisruptor(150, 50000);
        testFOPDisruptor(200, 30000);
        testFOPDisruptor(250, 30000);
        testFOPDisruptor(300, 30000);
        testFOPDisruptor(350, 20000);
        testFOPDisruptor(400, 20000);
        testFOPDisruptor(450, 20000);
        testFOPDisruptor(500, 10000);
        testFOPDisruptor(550, 10000);
        testFOPDisruptor(600, 10000);

        System.out.println("-----------stormpot object pool------------");
        testStormpot(50,  50000);
        testStormpot(100, 50000);
        testStormpot(150, 50000);
        testStormpot(200, 30000);
        testStormpot(250, 30000);
        testStormpot(300, 30000);
        testStormpot(350, 20000);
        testStormpot(400, 20000);
        testStormpot(450, 20000);
        testStormpot(500, 10000);
        testStormpot(550, 10000);
        testStormpot(600, 10000);

        System.out.println("-----------furious object pool------------");
        testFurious(50,  50000);
        testFurious(100, 50000);
        testFurious(150, 50000);
        testFurious(200, 30000);
        testFurious(250, 30000);
        testFurious(300, 30000);
        testFurious(350, 20000);
        testFurious(400, 20000);
        testFurious(450, 20000);
        testFurious(500, 10000);
        testFurious(550, 10000);
        testFurious(600, 10000);

        System.out.println("------------Apache commons pool-----------");
        // too slow, so less loops
        testCommon(50, 20000);
        testCommon(100, 10000);
        testCommon(150, 90000);
        testCommon(200, 8000);
        testCommon(250, 7000);
        testCommon(300, 6000);
        testCommon(350, 5000);
        testCommon(400, 4000);
        testCommon(450, 3000);
        testCommon(500, 2000);
        testCommon(550, 1000);
        testCommon(600, 1000);

        System.exit(0);

    }

    private static void testFOP(int workerCount, int loop) throws InterruptedException {
        new BenchmarkFastObjectPool(workerCount, loop).testAndPrint();
        cleanup();
    }

    private static void testFOPDisruptor(int workerCount, int loop) throws InterruptedException {
        new BenchmarkFastObjectPoolDisruptor(workerCount, loop).testAndPrint();
        cleanup();
    }

    private static void testStormpot(int workerCount, int loop) throws InterruptedException {
        new BenchmarkStormpot(workerCount, loop).testAndPrint();
        cleanup();
    }

    private static void testFurious(int workerCount, int loop) throws InterruptedException {
        new BenchmarkFurious(workerCount, loop).testAndPrint();
        cleanup();
    }

    private static void testCommon(int workerCount, int loop) throws Exception {
        new BenchmarkCommons(workerCount, loop).testAndPrint();
        cleanup();
    }

    private static void cleanup() {
        try {
            System.out.println("cleaning up ...");
            Thread.sleep(1000L * 2);
            System.gc();
            Thread.sleep(1000L * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
    }
}
