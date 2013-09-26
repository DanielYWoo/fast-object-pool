import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

/**
 * @author Daniel
 */
public class BenchmarkCommons {

    public BenchmarkCommons(int workerCount, int loop) throws Exception {
        double[] statsAvgRespTime = new double[workerCount];
        CountDownLatch latch = new CountDownLatch(workerCount);

        GenericObjectPool pool = new GenericObjectPool(new PoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                return new StringBuilder();
            }
            @Override
            public void destroyObject(Object o) throws Exception {
            }
            @Override
            public boolean validateObject(Object o) {
                return true;
            }
            @Override
            public void activateObject(Object o) throws Exception {
            }
            @Override
            public void passivateObject(Object o) throws Exception {
            }
        });
        pool.setMinIdle(25);
        pool.setMaxIdle(50);
        pool.setMaxActive(50);
        pool.setMinEvictableIdleTimeMillis(60 * 1000 * 5L);

        Worker[] workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(i, pool, latch, loop, statsAvgRespTime);
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

        private final int id;
        private final GenericObjectPool pool;
        private final CountDownLatch latch;
        private final int loop;
        private final double[] statsAvgRespTime;

        public Worker(int id, GenericObjectPool pool, CountDownLatch latch, int loop, double[] statsAvgRespTime) {
            this.id = id;
            this.pool = pool;
            this.latch = latch;
            this.loop = loop;
            this.statsAvgRespTime = statsAvgRespTime;
        }

        @Override public void run() {
            long t1 = System.currentTimeMillis();
            for (int i = 0; i < loop; i++) {
                StringBuilder obj = null;
                try {
                    obj = (StringBuilder) pool.borrowObject();
                    obj.append("x");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (obj != null) {
                        try {
                            pool.returnObject(obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            long t2 = System.currentTimeMillis();
            statsAvgRespTime[id] =  ((double) (t2 - t1)) / loop;
            latch.countDown();
        }
    }
}
