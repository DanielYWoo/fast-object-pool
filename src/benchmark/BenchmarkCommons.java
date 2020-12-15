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

    BenchmarkCommons(int workerCount, int borrowsPerLoop, int loop) throws Exception {
        super("common-pool", workerCount, borrowsPerLoop, loop);
        GenericObjectPool<StringBuilder> pool = new GenericObjectPool<>(new PooledObjectFactory<StringBuilder>() {
            @Override public PooledObject<StringBuilder> makeObject() throws Exception {
                created.incrementAndGet();
                return new DefaultPooledObject<>(new StringBuilder());
            }

            @Override public void destroyObject(PooledObject<StringBuilder> pooledObject) throws Exception { }

            @Override public boolean validateObject(PooledObject<StringBuilder> pooledObject) { return false; }

            @Override public void activateObject(PooledObject<StringBuilder> pooledObject) throws Exception { }

            @Override public void passivateObject(PooledObject<StringBuilder> pooledObject) throws Exception { }
        });
        pool.setMinIdle(256);
        pool.setMaxIdle(256);
        pool.setMaxTotal(256);
        pool.setMinEvictableIdleTimeMillis(60 * 1000 * 5L);

        this.workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this, i, loop, borrowsPerLoop, pool);
        }
    }

    private static class Worker extends BaseWorker {

        private final GenericObjectPool<StringBuilder> pool;

        Worker(Benchmark benchmark, int id, int loop, int borrowsPerLoop, GenericObjectPool<StringBuilder> pool) {
            super(benchmark, id, borrowsPerLoop, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
            List<StringBuilder> list = new ArrayList<>();
            try {
                for (int i = 0; i < borrowsPerLoop; i++) {
                    StringBuilder obj = pool.borrowObject(10);
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
