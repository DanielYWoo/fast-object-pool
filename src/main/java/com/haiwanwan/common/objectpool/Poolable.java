package com.haiwanwan.common.objectpool;

/**
 * @author Daniel
 */
public class Poolable<T> {

    private final T object;
    private final int partition;
    private long lastAccessTs;

    public Poolable(T t, int partition) {
        this.object = t;
        this.partition = partition;
    }

    public T getObject() {
        return object;
    }

    public int getPartition() {
        return partition;
    }

    public long getLastAccessTs() {
        return lastAccessTs;
    }

    public void setLastAccessTs(long lastAccessTs) {
        this.lastAccessTs = lastAccessTs;
    }

}
