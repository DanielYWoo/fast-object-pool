package cn.danielw.fop;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniel
 */
public class ObjectPoolPartition<T> {

    private static final Logger logger = Logger.getLogger(ObjectPoolPartition.class.getName());

    private final ObjectPool<T> pool;
    private final PoolConfig config;
    private final int partition;
    private final BlockingQueue<Poolable<T>> objectQueue;
    private final ObjectFactory<T> objectFactory;
    private int totalCount;

    public ObjectPoolPartition(ObjectPool<T> pool, int partition, PoolConfig config,
                               ObjectFactory<T> objectFactory, BlockingQueue<Poolable<T>> queue) {
        this.pool = pool;
        this.config = config;
        this.objectFactory = objectFactory;
        this.partition = partition;
        this.objectQueue = queue;
        for (int i = 0; i < config.getMinPartitionSize(); i++) {
            objectQueue.add(new Poolable<>(objectFactory.create(), pool, partition));
        }
        totalCount = config.getMinPartitionSize();
    }

    public BlockingQueue<Poolable<T>> getObjectQueue() {
        return objectQueue;
    }

    /**
     * @param delta the number to increase
     * @return the actual number of increased objects
     */
    @SuppressWarnings({"java:S112", "java:S2142"})
    public synchronized int increaseObjects(int delta) {
        if (delta + totalCount > config.getMaxPartitionSize()) {
            delta = config.getMaxPartitionSize() - totalCount;
        }
        try {
            for (int i = 0; i < delta; i++) {
                objectQueue.put(new Poolable<>(objectFactory.create(), pool, partition));
            }
            totalCount += delta;
            if (logger.isLoggable(Level.FINE))
                logger.fine("increase objects: count=" + totalCount + ", queue size=" + objectQueue.size());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return delta;
    }

    public synchronized boolean decreaseObject(Poolable<T> obj) {
        objectFactory.destroy(obj.getObject());
        totalCount--;
        return true;
    }

    public synchronized int getTotalCount() {
        return totalCount;
    }

    // set the scavenge interval carefully
    public synchronized void scavenge() throws InterruptedException {
        int delta = this.totalCount - config.getMinPartitionSize();
        if (delta <= 0) return;
        int removed = 0;
        long now = System.currentTimeMillis();
        Poolable<T> obj;
        while (delta-- > 0 && (obj = objectQueue.poll()) != null) {
            // performance trade off: delta always decrease even if the queue is empty,
            // so it could take several intervals to shrink the pool to the configured min value.
            if (logger.isLoggable(Level.FINE))
                logger.fine("obj=" + obj + ", now-last=" + (now - obj.getLastAccessTs()) + ", max idle=" +
                    config.getMaxIdleMilliseconds());
            if (now - obj.getLastAccessTs() > config.getMaxIdleMilliseconds() &&
                    ThreadLocalRandom.current().nextDouble(1) < config.getScavengeRatio()) {
                decreaseObject(obj); // shrink the pool size if the object reaches max idle time
                removed++;
            } else {
                objectQueue.put(obj); //put it back
            }
        }
        if (removed > 0 && logger.isLoggable(Level.FINE)) logger.fine(removed + " objects were scavenged.");
    }

    public synchronized int shutdown() {
        int removed = 0;
        long startTs = System.currentTimeMillis();
        while (this.totalCount > 0 && System.currentTimeMillis() - startTs < config.getShutdownWaitMilliseconds()) {
            Poolable<T> obj = objectQueue.poll();
            if (obj != null) {
                decreaseObject(obj);
                removed++;
            }
        }
        return removed;
    }
}
