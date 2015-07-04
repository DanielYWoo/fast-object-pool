package com.haiwanwan.common.objectpool;

/**
 * @author Daniel
 */
public class PoolConfig {

    public static final int DEFAULT_MIN_SIZE = 5;
    public static final int DEFAULT_MAX_SIZE = 20;

    private int maxWaitMilliseconds = 5000; // when pool is full, wait at most 5 seconds, then throw an exception
    private int maxIdleMilliseconds = 300000; // objects idle for 5 minutes will be destroyed to shrink the pool size
    private int minSize = DEFAULT_MIN_SIZE;
    private int maxSize = DEFAULT_MAX_SIZE;
    private int partitionSize = 4;
    private int scavengeIntervalMilliseconds = 1000 * 60 * 2;

    public int getMaxWaitMilliseconds() {
        return maxWaitMilliseconds;
    }

    public void setMaxWaitMilliseconds(int maxWaitMilliseconds) {
        this.maxWaitMilliseconds = maxWaitMilliseconds;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxIdleMilliseconds() {
        return maxIdleMilliseconds;
    }

    public void setMaxIdleMilliseconds(int maxIdleMilliseconds) {
        this.maxIdleMilliseconds = maxIdleMilliseconds;
    }

    public int getPartitionSize() {
        return partitionSize;
    }

    public void setPartitionSize(int partitionSize) {
        this.partitionSize = partitionSize;
    }

    public int getScavengeIntervalMilliseconds() {
        return scavengeIntervalMilliseconds;
    }

    /**
     * @param scavengeIntervalMilliseconds set it to zero if you don't want to automatically shrink your pool.
     *                                     This is useful for fixed-size pool, or pools don't increase too much.
     */
    public void setScavengeIntervalMilliseconds(int scavengeIntervalMilliseconds) {
        this.scavengeIntervalMilliseconds = scavengeIntervalMilliseconds;
    }
}
