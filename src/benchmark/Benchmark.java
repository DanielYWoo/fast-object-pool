import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

/**
 * @author Daniel
 */
public class Benchmark {

    final private long workerCount;
    final private long loop;
    final CountDownLatch latch;
    static double[] statsAvgRespTime;
    static double[] statsAvgBorrow;
    static double[] statsAvgReturn;

    public Benchmark(int workerCount, int loop) {
        this.workerCount = workerCount;
        this.loop = loop;
        this.latch = new CountDownLatch(workerCount);
        statsAvgRespTime = new double[workerCount];
        statsAvgBorrow = new double[workerCount];
        statsAvgReturn = new double[workerCount];
    }

    protected void testAndPrint(BaseWorker[] workers) throws InterruptedException {
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
        System.out.println("Average Response Time:" + new DecimalFormat("0.00").format(stats / workerCount));
        stats = 0;
        for (int i = 0; i < workerCount; i++) {
            stats += statsAvgBorrow[i];
        }
        System.out.println("Average Borrow Time:" + new DecimalFormat("0.00").format(stats / workerCount));
        stats = 0;
        for (int i = 0; i < workerCount; i++) {
            stats += statsAvgReturn[i];
        }
        System.out.println("Average Return Time:" + new DecimalFormat("0.00").format(stats / workerCount));
        System.out.println("Average Througput Per Second:" + new DecimalFormat("0").format(( loop * workerCount * 1000 ) / (t2 - t1) ));
    }

    public static void main(String[] args) throws Exception {

        System.out.println("-----------warm up------------");
        new BenchmarkFastObjectPool(50,  1000);
//        new BenchmarkStormpot(50,  1000);
        new BenchmarkCommons(50,  1000);

        System.out.println("-----------fast object pool------------");
        new BenchmarkFastObjectPool(50,  50000);
        new BenchmarkFastObjectPool(100, 50000);
        new BenchmarkFastObjectPool(150, 50000);
        new BenchmarkFastObjectPool(200, 30000);
        new BenchmarkFastObjectPool(250, 30000);
        new BenchmarkFastObjectPool(300, 30000);
        new BenchmarkFastObjectPool(350, 20000);
        new BenchmarkFastObjectPool(400, 20000);
        new BenchmarkFastObjectPool(450, 20000);
        new BenchmarkFastObjectPool(500, 10000);
        new BenchmarkFastObjectPool(550, 10000);
        new BenchmarkFastObjectPool(600, 10000);
/*
        System.out.println("-----------storm pot object pool------------");
        new BenchmarkStormpot(50,  50000);
        new BenchmarkStormpot(100, 50000);
        new BenchmarkStormpot(150, 50000);
        new BenchmarkStormpot(200, 30000);
        new BenchmarkStormpot(250, 30000);
        new BenchmarkStormpot(300, 30000);
        new BenchmarkStormpot(350, 20000);
        new BenchmarkStormpot(400, 20000);
        new BenchmarkStormpot(450, 20000);
        new BenchmarkStormpot(500, 10000);
        new BenchmarkStormpot(550, 10000);
        new BenchmarkStormpot(600, 10000);
*/

        System.out.println("------------Apache commons pool-----------");
        // too slow, so less loops
        new BenchmarkCommons(50, 20000);
        new BenchmarkCommons(100, 10000);
        new BenchmarkCommons(150, 90000);
        new BenchmarkCommons(200, 8000);
        new BenchmarkCommons(250, 7000);
        new BenchmarkCommons(300, 6000);
        new BenchmarkCommons(350, 5000);
        new BenchmarkCommons(400, 4000);
        new BenchmarkCommons(450, 3000);
        new BenchmarkCommons(500, 2000);
        new BenchmarkCommons(550, 1000);
        new BenchmarkCommons(600, 1000);

    }
}
