package com.haiwanwan.common.objectpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Daniel
 */
public class ObjectPoolPartition<T> {

    private final PoolConfig config;
    private final int partition;
    private final BlockingQueue<Poolable<T>> objectQueue;
    private final ObjectFactory<T> objectFactory;
    private int count;

    public ObjectPoolPartition(int partition, PoolConfig config, ObjectFactory<T> objectFactory) throws InterruptedException {
        this.config = config;
        this.objectFactory = objectFactory;
        this.partition = partition;
        this.objectQueue = new ArrayBlockingQueue<>(config.getMaxSize());
        for (int i = 0; i < config.getMinSize(); i++) {
            objectQueue.put(new Poolable<>(objectFactory.create(), partition));
        }
        count = config.getMinSize();
    }

    public BlockingQueue<Poolable<T>> getObjectQueue() {
        return objectQueue;
    }

    public synchronized int increaseObjects(int delta) {
        if (delta + count > config.getMaxSize()) {
            delta = config.getMaxSize() - count;
        }
        try {
            for (int i = 0; i < delta; i++) {
                objectQueue.put(new Poolable<>(objectFactory.create(), partition));
            }
            count += delta;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return delta;
    }

    public synchronized boolean decreaseObject(Poolable<T> obj) {
        objectFactory.destroy(obj.getObject());
        count--;
        return true;
    }

}
