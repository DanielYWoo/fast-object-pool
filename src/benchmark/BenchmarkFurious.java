import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.PoolableObjectBase;

/**
 * @author Daniel
 */
public class BenchmarkFurious extends Benchmark {

    BenchmarkFurious(int workerCount, int loop) throws InterruptedException {
        super(workerCount, loop);

        // Create your PoolSettings with an instance of PoolableObject
        PoolSettings<StringBuilder> poolSettings = new PoolSettings<>(new PoolableObjectBase<StringBuilder>() {
            @Override
            public StringBuilder make() {
                created.incrementAndGet();
                return new StringBuilder();
            }
            @Override
            public void activate(StringBuilder t) {
                t.setLength(0);
            }
        });
        // Add some settings
        poolSettings.min(256).max(256);

        // Get the objectPool instance using a Singleton Design Pattern is a good idea
        ObjectPool<StringBuilder> pool = poolSettings.pool();

        workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this, i, loop, pool);
        }
    }

    private static class Worker extends BaseWorker {

        private final ObjectPool<StringBuilder> pool;

        Worker(Benchmark benchmark, int id, long loop, ObjectPool<StringBuilder> pool) {
            super(benchmark, id, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
            StringBuilder buffer = null;
            try {
                buffer = pool.getObj();
                buffer.append("x");
            } catch (PoolException e) {
                err++;
            } finally {
                if (buffer != null) {
                    try {
                        pool.returnObj(buffer);
                    } catch (Exception e) {
                        err++;
                    }
                }
            }

        }

    }
}
