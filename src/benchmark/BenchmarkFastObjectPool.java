import cn.danielw.fop.ObjectFactory;
import cn.danielw.fop.ObjectPool;
import cn.danielw.fop.PoolConfig;
import cn.danielw.fop.Poolable;

/**
 * @author Daniel
 */
public class BenchmarkFastObjectPool extends Benchmark {

    BenchmarkFastObjectPool(int workerCount, int loop) throws InterruptedException {
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
        workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this, i, loop, pool);
        }
    }

    protected static class Worker extends BaseWorker {

        private final ObjectPool<StringBuilder> pool;

        Worker(Benchmark benchmark, int id, long loop, ObjectPool<StringBuilder> pool) {
            super(benchmark, id, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
            Poolable<StringBuilder> obj = null;
            try {
                obj = pool.borrowObject();
                obj.getObject().append("x");
            } catch (Exception e) {
                err++;
            } finally {
                if (obj != null) {
                    try {
                        pool.returnObject(obj);
                    } catch (Exception e) {
                        err++;
                    }
                }
            }
        }
    }
}
