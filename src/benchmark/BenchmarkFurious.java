import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.PoolableObjectBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel
 */
public class BenchmarkFurious extends Benchmark {

    BenchmarkFurious(int workerCount, int borrows, int loop) throws InterruptedException {
        super("furious", workerCount, borrows, loop);

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
            workers[i] = new Worker(this, i, borrows, loop, pool);
        }
    }

    private static class Worker extends BaseWorker {

        private final ObjectPool<StringBuilder> pool;

        Worker(Benchmark benchmark, int id, int borrows, long loop, ObjectPool<StringBuilder> pool) {
            super(benchmark, id, borrows, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
            List<StringBuilder> list = new ArrayList<>();
            try {
                for (int i = 0; i < borrowsPerLoop; i++) {
                    StringBuilder obj = pool.getObj(); // NO timeout at method level
                    obj.append("X");
                    list.add(obj);
                }
            } catch (Exception e) {
                err++;
            } finally {
                list.forEach(o -> {
                    try {
                        pool.returnObj(o);
                    } catch (Exception e) {
                        err++;
                    }
                });
            }
        }
    }
}
