package cn.danielw.fop;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;

import java.util.concurrent.BlockingQueue;

public class DisruptorObjectPool<T> extends ObjectPool<T> {

    public DisruptorObjectPool(PoolConfig poolConfig, ObjectFactoryRaw<T> objectFactory) {
        super(poolConfig, objectFactory);
    }

    public DisruptorObjectPool(PoolConfig poolConfig, ObjectFactoryRaw<T> objectFactory, boolean init) {
        super(poolConfig, objectFactory, init);
    }

    @Override
    protected BlockingQueue<Poolable<T>> createBlockingQueue(PoolConfig config) {
        return new DisruptorBlockingQueue<>(config.getMaxPartitionSize());
    }

}
