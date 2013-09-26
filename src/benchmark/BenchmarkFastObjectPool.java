import com.haiwanwan.common.objectpool.ObjectFactory;
import com.haiwanwan.common.objectpool.ObjectPool;
import com.haiwanwan.common.objectpool.PoolConfig;
import com.haiwanwan.common.objectpool.Poolable;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

/**
 * @author Daniel
 */
public class BenchmarkFastObjectPool {

    public BenchmarkFastObjectPool(int workerCount, int loop) throws InterruptedException {
        double[] statsAvgRespTime = new double[workerCount];
        CountDownLatch latch = new CountDownLatch(workerCount);

        PoolConfig config = new PoolConfig();
        config.setPartitionSize(5);
        config.setMaxSize(10);
        config.setMinSize(5);
        config.setMaxIdleMilliseconds(60 * 1000 * 5);

        ObjectFactory<StringBuilder> factory = new ObjectFactory<StringBuilder>() {
            @Override
            public StringBuilder create() {
                return new StringBuilder();
            }
            @Override
            public void destroy(StringBuilder o) {
            }
            @Override
            public boolean validate(StringBuilder o) {
                return true;
            }
        };
        ObjectPool pool = new ObjectPool(config, factory);
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
        private final ObjectPool<StringBuilder> pool;
        private final CountDownLatch latch;
        private final int loop;
        private final double[] statsAvgRespTime;

        public Worker(int id, ObjectPool<StringBuilder> pool, CountDownLatch latch, int loop, double[] statsAvgRespTime) {
            this.id = id;
            this.pool = pool;
            this.latch = latch;
            this.loop = loop;
            this.statsAvgRespTime = statsAvgRespTime;
        }

        @Override public void run() {
            long t1 = System.currentTimeMillis();
            for (int i = 0; i < loop; i++) {
                Poolable<StringBuilder> obj = null;
                try {
                    obj = pool.borrowObject();
                    obj.getObject().append("x");
                } finally {
                    if (obj != null) {
                        pool.returnObject(obj);
                    }
                }
            }
            long t2 = System.currentTimeMillis();
            statsAvgRespTime[id] =  ((double) (t2 - t1)) / loop;
            latch.countDown();
        }
    }
}
