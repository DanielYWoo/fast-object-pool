
import cn.danielw.fop.ObjectFactory;
import cn.danielw.fop.ObjectPool;
import cn.danielw.fop.PoolConfig;
import cn.danielw.fop.Poolable;

import java.util.concurrent.CountDownLatch;

/**
 * @author Daniel
 */
public class BenchmarkFastObjectPool extends Benchmark {

    public BenchmarkFastObjectPool(int workerCount, int loop) throws InterruptedException {
        super(workerCount, loop);
        PoolConfig config = new PoolConfig();
        config.setPartitionSize(16);
        config.setMaxSize(16);
        config.setMinSize(16);
        config.setMaxIdleMilliseconds(60 * 1000 * 5);

        ObjectFactory<StringBuilder> factory = new ObjectFactory<StringBuilder>() {
            @Override
            public StringBuilder create() {
                created.incrementAndGet();
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
        ObjectPool<StringBuilder> pool = new ObjectPool<>(config, factory);
        Worker[] workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this, i, latch, loop, pool);
        }
        testAndPrint(workers);
    }

    private static class Worker extends BaseWorker {

        private final ObjectPool<StringBuilder> pool;

        public Worker(Benchmark benchmark, int id, CountDownLatch latch, long loop, ObjectPool<StringBuilder> pool) {
            super(benchmark, id, latch, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
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
    }
}
