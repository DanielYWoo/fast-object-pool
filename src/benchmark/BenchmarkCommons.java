import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel
 */
public class BenchmarkCommons extends Benchmark {

    BenchmarkCommons(int workerCount, int borrowsPerLoop, int loop, int simulateBlockingMs) {
        super("common-pool", workerCount, borrowsPerLoop, loop);
        GenericObjectPool<StringBuilder> pool = new GenericObjectPool<>(new PooledObjectFactory<>() {
            @Override
            public PooledObject<StringBuilder> makeObject() {
                created.incrementAndGet();
                return new DefaultPooledObject<>(new StringBuilder());
            }

            @Override
            public void destroyObject(PooledObject<StringBuilder> pooledObject) {
            }

            @Override
            public boolean validateObject(PooledObject<StringBuilder> pooledObject) {
                return false;
            }

            @Override
            public void activateObject(PooledObject<StringBuilder> pooledObject) {
            }

            @Override
            public void passivateObject(PooledObject<StringBuilder> pooledObject) {
            }
        });
        pool.setMinIdle(256);
        pool.setMaxIdle(256);
        pool.setMaxTotal(256);
        pool.setMinEvictableIdleTimeMillis(60 * 1000 * 5L);

        this.workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this, i, loop, borrowsPerLoop, simulateBlockingMs, pool);
        }
    }

    private static class Worker extends BaseWorker {

        private final GenericObjectPool<StringBuilder> pool;

        Worker(Benchmark benchmark, int id, int loop, int borrowsPerLoop, int simulateBlockingMs, GenericObjectPool<StringBuilder> pool) {
            super(benchmark, id, borrowsPerLoop, loop, simulateBlockingMs);
            this.pool = pool;
        }

        @Override public void doSomething() {
            List<StringBuilder> list = new ArrayList<>();
            try {
                for (int i = 0; i < borrowsPerLoop; i++) {
                    StringBuilder obj = pool.borrowObject(10);
                    obj.append("X");
                    if (simulateBlockingMs > 0) Thread.sleep(simulateBlockingMs); // simulate thread blocking
                    list.add(obj);
                }
            } catch (Exception e) {
                err++;
            } finally {
                list.forEach(o -> {
                    try {
                        pool.returnObject(o);
                    } catch (Exception e) {
                        err++;
                    }
                });
            }
        }

    }
}
