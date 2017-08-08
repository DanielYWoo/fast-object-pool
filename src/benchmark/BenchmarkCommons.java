import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * @author Daniel
 */
public class BenchmarkCommons extends Benchmark {

    BenchmarkCommons(int workerCount, int loop) throws Exception {
        super(workerCount, loop);
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
            workers[i] = new Worker(this, i, loop, pool);
        }
    }

    private static class Worker extends BaseWorker {

        private final GenericObjectPool<StringBuilder> pool;

        Worker(Benchmark benchmark, int id, int loop, GenericObjectPool<StringBuilder> pool) {
            super(benchmark, id, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
            StringBuilder obj = null;
            try {
                obj = pool.borrowObject();
                obj.append("x");
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
