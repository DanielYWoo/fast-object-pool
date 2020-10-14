package cn.danielw.fop;

/**
 * @author Daniel
 */
public class PoolConfig {

    private int maxWaitMilliseconds = 5000; // when pool is full, wait at most 5 seconds, then throw an exception
    private int maxIdleMilliseconds = 300000; // objects idle for 5 minutes will be destroyed to shrink the pool size
    private int minSize = 5;
    private int maxSize = 20;
    private int partitionSize = 4;
    private int scavengeIntervalMilliseconds = 1000 * 60 * 2;
    private double scavengeRatio = 0.5; // to avoid to clean up all connections in the pool at the same time

    public int getMaxWaitMilliseconds() {
        return maxWaitMilliseconds;
    }

    /**
     * this is only used for blocking call to <code>borrowObject(true)</code>.
     * @param maxWaitMilliseconds how long to block
     * @return the pool config
     */
    public PoolConfig setMaxWaitMilliseconds(int maxWaitMilliseconds) {
        if (maxWaitMilliseconds <= 0) {
            throw new IllegalArgumentException("Cannot set max wait time to a negative number " + maxWaitMilliseconds);
        }
        this.maxWaitMilliseconds = maxWaitMilliseconds;
        return this;
    }

    public int getMinSize() {
        return minSize;
    }

    public PoolConfig setMinSize(int minSize) {
        this.minSize = minSize;
        return this;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public PoolConfig setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public int getMaxIdleMilliseconds() {
        return maxIdleMilliseconds;
    }

    public PoolConfig setMaxIdleMilliseconds(int maxIdleMilliseconds) {
        this.maxIdleMilliseconds = maxIdleMilliseconds;
        return this;
    }

    public int getPartitionSize() {
        return partitionSize;
    }

    public PoolConfig setPartitionSize(int partitionSize) {
        this.partitionSize = partitionSize;
        return this;
    }

    public int getScavengeIntervalMilliseconds() {
        return scavengeIntervalMilliseconds;
    }

    /**
     * @param scavengeIntervalMilliseconds set it to zero if you don't want to automatically shrink your pool.
     *                                     This is useful for fixed-size pool, or pools don't increase too much.
     * @return the pool config
     */
    public PoolConfig setScavengeIntervalMilliseconds(int scavengeIntervalMilliseconds) {
        if (scavengeIntervalMilliseconds < 5000) {
            throw new IllegalArgumentException("Cannot set interval too short (" + scavengeIntervalMilliseconds +
                    "), must be at least 5 seconds");
        }
        this.scavengeIntervalMilliseconds = scavengeIntervalMilliseconds;
        return this;
    }

    public double getScavengeRatio() {
        return scavengeRatio;
    }

    /**
     *  Each time we shrink a pool, we only scavenge some of the objects to avoid an empty pool
     * @param scavengeRatio must be a double between (0, 1]
     * @return the pool config
     */
    public PoolConfig setScavengeRatio(double scavengeRatio) {
        if (scavengeRatio <= 0 || scavengeRatio > 1) {
            throw new IllegalArgumentException("Invalid scavenge ratio: " + scavengeRatio);
        }
        this.scavengeRatio = scavengeRatio;
        return this;
    }
}
