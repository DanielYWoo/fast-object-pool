import stormpot.*;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniel
 */
public class BenchmarkStormpot {

    static class MyPoolable implements stormpot.Poolable {

        private StringBuilder test;

        public MyPoolable() {
            test = new StringBuilder();
        }

        public StringBuilder getTest() {
            return test;
        }

        @Override
        public void release() {

        }
    }

    private static double[] statsAvgRespTime;

    public BenchmarkStormpot(int workerCount, int loop) throws InterruptedException {
        statsAvgRespTime = new double[workerCount];
        CountDownLatch latch = new CountDownLatch(workerCount);

        Config<MyPoolable> config = new Config<>().setAllocator(new Allocator<MyPoolable>() {
            @Override
            public MyPoolable allocate(Slot slot) throws Exception {
                return new MyPoolable();
            }

            @Override
            public void deallocate(MyPoolable x) throws Exception {

            }
        }).setSize(50);
        /*
        config.setExpiration(new Expiration<MyPoolable>() {
            @Override
            public boolean hasExpired(SlotInfo<? extends MyPoolable> slotInfo) throws Exception {
                // how to support max idle?
                return slotInfo.getAgeMillis() > 60 * 10000 * 5;
            }
        });
        */
        Pool<MyPoolable> pool = new BlazePool<>(config);


        Worker[] workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(i, pool, latch, loop);
        }
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
        System.out.println("Average Response Time:" + new DecimalFormat("0").format(stats / workerCount));
        System.out.println("Average Througput Per Second:" + new DecimalFormat("0").format(( (double) loop * workerCount * 1000 ) / (t2 - t1) ));
    }

    private static class Worker extends Thread {

        private static final Timeout TIMEOUT = new Timeout(1, TimeUnit.HOURS);
        private final int id;
        private final Pool<MyPoolable> pool;
        private final CountDownLatch latch;
        private final int loop;

        public Worker(int id, Pool<MyPoolable> pool, CountDownLatch latch, int loop) {
            this.id = id;
            this.pool = pool;
            this.latch = latch;
            this.loop = loop;
        }

        @Override public void run() {
            long t1 = System.currentTimeMillis();
            for (int i = 0; i < loop; i++) {
                MyPoolable obj = null;
                try {
                    obj = pool.claim(TIMEOUT);
                    // todo: check NPE
                    obj.getTest().append("x");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (obj != null) {
                        obj.release();
                    }
                }
            }
            long t2 = System.currentTimeMillis();
            synchronized (statsAvgRespTime) {
                statsAvgRespTime[id] =  ((double) (t2 - t1)) / loop;
            }
            latch.countDown();
        }
    }
}
