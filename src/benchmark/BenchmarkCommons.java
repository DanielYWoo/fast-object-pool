import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.concurrent.CountDownLatch;

/**
 * @author Daniel
 */
public class BenchmarkCommons extends Benchmark {

    public BenchmarkCommons(int workerCount, int loop) throws Exception {
        super(workerCount, loop);
        GenericObjectPool pool = new GenericObjectPool(new PooledObjectFactory() {
            @Override public PooledObject makeObject() throws Exception {
                return new DefaultPooledObject(new StringBuilder());
            }

            @Override public void destroyObject(PooledObject pooledObject) throws Exception { }

            @Override public boolean validateObject(PooledObject pooledObject) { return false; }

            @Override public void activateObject(PooledObject pooledObject) throws Exception { }

            @Override public void passivateObject(PooledObject pooledObject) throws Exception { }
        });
        pool.setMinIdle(25);
        pool.setMaxIdle(50);
        pool.setMaxTotal(50);
        pool.setMinEvictableIdleTimeMillis(60 * 1000 * 5L);

        Worker[] workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this, i, latch, loop, pool);
        }

        testAndPrint(workers);
    }

    private static class Worker extends BaseWorker {

        private final GenericObjectPool pool;

        public Worker(Benchmark benchmark, int id, CountDownLatch latch, int loop, GenericObjectPool pool) {
            super(benchmark, id, latch, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
            StringBuilder obj = null;
            try {
                long tp1 = System.currentTimeMillis();
                obj = (StringBuilder) pool.borrowObject();
                tb += System.currentTimeMillis() - tp1;
                obj.append("x");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (obj != null) {
                    try {
                        long tp3 = System.currentTimeMillis();
                        pool.returnObject(obj);
                        tr += System.currentTimeMillis() - tp3;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
