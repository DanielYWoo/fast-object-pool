import cn.danielw.fop.DisruptorObjectPool;
import cn.danielw.fop.ObjectFactory;
import cn.danielw.fop.PoolConfig;

/**
 * @author Daniel
 */
public class BenchmarkFastObjectPoolDisruptor extends BenchmarkFastObjectPool {

    BenchmarkFastObjectPoolDisruptor(int workerCount, int loop) throws InterruptedException {
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
        DisruptorObjectPool<StringBuilder> pool = new DisruptorObjectPool<>(config, factory);
        workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this, i, loop, pool);
        }
    }

}
