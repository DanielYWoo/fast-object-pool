import cn.danielw.fop.DisruptorObjectPool;
import cn.danielw.fop.ObjectFactory;
import cn.danielw.fop.PoolConfig;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Daniel
 */
public class BenchmarkFastObjectPoolDisruptor extends Benchmark {

    BenchmarkFastObjectPoolDisruptor(int workerCount, int borrows, int loop) throws InterruptedException {
        super("fop", workerCount, borrows, loop);
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
        DisruptorObjectPool<StringBuilder> pool = new DisruptorObjectPool<>(config, factory);
        workers = new BenchmarkFastObjectPool.Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new BenchmarkFastObjectPool.Worker(this, i, borrows, loop, pool);
        }
    }

}
