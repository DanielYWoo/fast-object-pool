package cn.danielw.fop;

import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;

import java.util.concurrent.BlockingQueue;

/**
* This pool has disruptor as the underlying blocking queue.
*/
public class DisruptorObjectPool<T> extends ObjectPool<T> {

    /**
     * build a disruptor-based pool for better performance.
     */
    public DisruptorObjectPool(PoolConfig poolConfig, ObjectFactory<T> objectFactory) {
        super(poolConfig, objectFactory);
    }

    /**
     * create a disruptor-based blocking queue for better performance
     */
    @Override
    protected BlockingQueue<Poolable<T>> createBlockingQueue(PoolConfig config) {
        return new DisruptorBlockingQueue<>(config.getMaxSize());
    }

}
