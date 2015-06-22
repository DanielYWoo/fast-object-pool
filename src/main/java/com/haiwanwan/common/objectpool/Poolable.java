package com.haiwanwan.common.objectpool;

/**
 * @author Daniel
 * AutoCloseable.close() is not idemponent, so don't close it multiple times!
 */
public class Poolable<T> implements AutoCloseable {

    private final T object;
    private ObjectPool<T> pool;
    private final int partition;
    private long lastAccessTs;

    public Poolable(T t, ObjectPool<T> pool, int partition) {
        this.object = t;
        this.pool = pool;
        this.partition = partition;
    }

    public T getObject() {
        return object;
    }

    public ObjectPool<T> getPool() {
        return pool;
    }

    public int getPartition() {
        return partition;
    }

    public void returnObject() {
        pool.returnObject(this);
    }

    public long getLastAccessTs() {
        return lastAccessTs;
    }

    public void setLastAccessTs(long lastAccessTs) {
        this.lastAccessTs = lastAccessTs;
    }

    /**
     * This method is not idemponent, don't call it twice, which will return the object twice to the pool and cause severe problems.
     */
    @Override
    public void close() {
        this.returnObject();
    }
}
