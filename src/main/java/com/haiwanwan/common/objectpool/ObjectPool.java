package com.haiwanwan.common.objectpool;

import java.util.concurrent.TimeUnit;

/**
 * @author Daniel
 */
public class ObjectPool<T> {

    protected final PoolConfig config;
    protected final ObjectFactory<T> factory;
    protected final ObjectPoolPartition<T>[] partitions;
    private boolean shudown;

    public ObjectPool(PoolConfig poolConfig, ObjectFactory<T> objectFactory) {
        this.config = poolConfig;
        this.factory = objectFactory;
        this.partitions = new ObjectPoolPartition[config.getPartitionSize()];
        try {
            for (int i = 0; i < config.getPartitionSize(); i++) {
                partitions[i] = new ObjectPoolPartition<>(i, config, objectFactory);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Poolable<T> borrowObject() {
        return borrowObject(true);
    }

    public Poolable<T> borrowObject(boolean blocking) {
        for (int i = 0; i < 3; i++) { // try at most three times
            Poolable<T> result = getObject(blocking);
            if (factory.validate(result.getObject())) {
                return result;
            } else {
                this.partitions[result.getPartition()].decreaseObject(result);
            }
        }
        throw new RuntimeException("Cannot find a valid object");
    }

    private Poolable<T> getObject(boolean blocking) {
        if (shudown) {
            throw new IllegalStateException("Your pool is shutting down");
        }
        int partition = (int) (Thread.currentThread().getId() % this.config.getPartitionSize());
        ObjectPoolPartition<T> subPool = this.partitions[partition];
        Poolable<T> freeObject = subPool.getObjectQueue().poll();
        if (freeObject == null) {
            // try other partitions
            for (int i = 0; i < this.partitions.length; i++) {
                if (i != partition) {
                    freeObject = this.partitions[i].getObjectQueue().poll();
                    if (freeObject != null) {
                        break; // found one, stop
                    }
                }
            }
        }
        if (freeObject == null) {
            // increase objects and return one, it will return null if reach max size
            subPool.increaseObjects(1);
            try {
                if (blocking) {
                    freeObject = subPool.getObjectQueue().take();
                } else {
                    freeObject = subPool.getObjectQueue().poll(config.getMaxWaitMilliseconds(), TimeUnit.MILLISECONDS);
                    if (freeObject == null) {
                        throw new RuntimeException("Cannot get a free object from the pool");
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e); // will never happen
            }
        }
        freeObject.setLastAccessTs(System.currentTimeMillis());
        return freeObject;
    }

    public void returnObject(Poolable<T> obj) {
        ObjectPoolPartition<T> subPool = this.partitions[obj.getPartition()];
        if (System.currentTimeMillis() - obj.getLastAccessTs() > config.getMaxIdleMilliseconds()) {
            subPool.decreaseObject(obj); // shrink the pool size if the object reaches max idle time
        } else {
            try {
                subPool.getObjectQueue().put(obj);
            } catch (InterruptedException e) {
                throw new RuntimeException(e); // impossible for now
            }
        }
    }

    public void close() {
        shudown = true;
        //TODO: gracefully destroy all objects in the pool
    }

}
