import cn.danielw.fop.DisruptorObjectPool;
import cn.danielw.fop.ObjectFactory;
import cn.danielw.fop.PoolConfig;

/**
 * @author Daniel
 */
public class BenchmarkFastObjectPoolDisruptor extends Benchmark {

    BenchmarkFastObjectPoolDisruptor(int workerCount, int borrows, int loop, int simulateBlockingMs) {
        super("fop", workerCount, borrows, loop);
        PoolConfig config = new PoolConfig();
        config.setPartitionSize(32);
        config.setMaxSize(16);
        config.setMinSize(16);
        config.setMaxIdleMilliseconds(60 * 1000 * 5);
        config.setMaxWaitMilliseconds(10);

        ObjectFactory<StringBuilder> factory = new ObjectFactory<>() {
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
            workers[i] = new BenchmarkFastObjectPool.Worker(this, i, borrows, loop, simulateBlockingMs, pool);
        }
    }

}
