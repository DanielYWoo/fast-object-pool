import cn.danielw.fop.ObjectFactory;
import cn.danielw.fop.ObjectPool;
import cn.danielw.fop.PoolConfig;
import cn.danielw.fop.Poolable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel
 */
public class BenchmarkFastObjectPool extends Benchmark {

    BenchmarkFastObjectPool(String name, int workerCount, int borrows, int loop) throws InterruptedException {
        super(name, workerCount, borrows, loop);
        PoolConfig config = new PoolConfig();
        config.setPartitionSize(16);
        config.setMaxSize(16);
        config.setMinSize(16);
        config.setMaxIdleMilliseconds(60 * 1000 * 5);
        config.setMaxWaitMilliseconds(10);


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
            workers[i] = new Worker(this, i, borrows, loop, pool);
        }
    }

    protected static class Worker extends BaseWorker {

        private final ObjectPool<StringBuilder> pool;

        Worker(Benchmark benchmark, int id, int borrows, long loop, ObjectPool<StringBuilder> pool) {
            super(benchmark, id, borrows, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
            List<Poolable<StringBuilder>> list = new ArrayList<>();
            try {
                for (int i = 0; i < borrowsPerLoop; i++) {
                    Poolable<StringBuilder> obj = pool.borrowObject(false);
                    obj.getObject().append("X");
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
