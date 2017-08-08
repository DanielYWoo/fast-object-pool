package cn.danielw.fop;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;

import java.util.concurrent.BlockingQueue;

public class DisruptorObjectPool<T> extends ObjectPool<T> {

    public DisruptorObjectPool(PoolConfig poolConfig, ObjectFactory<T> objectFactory) {
        super(poolConfig, objectFactory);
    }

    @Override
    protected BlockingQueue<Poolable<T>> createBlockingQueue(PoolConfig config) {
        return new DisruptorBlockingQueue<>(config.getMaxSize());
    }

}
