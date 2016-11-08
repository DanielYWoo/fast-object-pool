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

    public double getScavengeRatio() {
        return scavengeRatio;
    }

    /**
     *  Each time we shrink a pool, we only scavenge some of the objects to avoid an empty pool
     * @param scavengeRatio must be a double between (0, 1]
     */
    public void setScavengeRatio(double scavengeRatio) {
        if (scavengeRatio <= 0 || scavengeRatio > 1) {
            throw new IllegalArgumentException("Invalid scavenge ratio: " + scavengeRatio);
        }
        this.scavengeRatio = scavengeRatio;
    }
}
