package cn.danielw.fop;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniel
 */
public class ObjectPool<T> {

    private static final Logger logger = Logger.getLogger(ObjectPool.class.getCanonicalName());

    private final PoolConfig config;
    private final ObjectFactory<T> factory;
    private final ObjectPoolPartition<T>[] partitions;
    private Scavenger scavenger;
    private volatile boolean shuttingDown;

    public ObjectPool(PoolConfig poolConfig, ObjectFactory<T> objectFactory) {
        this.config = poolConfig;
        this.factory = objectFactory;
        this.partitions = new ObjectPoolPartition[config.getPartitionSize()];
        for (int i = 0; i < config.getPartitionSize(); i++) {
            partitions[i] = new ObjectPoolPartition<>(this, i, config, objectFactory, createBlockingQueue(poolConfig));
        }
        if (config.getScavengeIntervalMilliseconds() > 0) {
            this.scavenger = new Scavenger();
            this.scavenger.start();
        }
    }

    /**
     * the pool needs a blocking queue for consumers to wait for an available object. The queue can be in different
     * implementations in subclasses.
     */
    protected BlockingQueue<Poolable<T>> createBlockingQueue(PoolConfig poolConfig) {
        return new ArrayBlockingQueue<>(poolConfig.getMaxSize());
    }

    /**
     * borrow an object from the pool. the call will be blocked for at most <code>PoolConfig.maxWaitMilliseconds</code>
     * before throwing an Exception
     * @return the object
     */
    public Poolable<T> borrowObject() {
        return borrowObject(true);
    }

    /**
     * borrow an object from the pool
     * @param noTimeout if true, the call will be blocked until one is available;
     *                  if false, the call will be blocked for at most <code>PoolConfig.maxWaitMilliseconds</code>
     *                  before throwing an Exception
     * @return the object
     */
    public Poolable<T> borrowObject(boolean noTimeout) {
        for (int i = 0; i < 3; i++) { // try at most three times
            Poolable<T> result = getObject(noTimeout);
            if (factory.validate(result.getObject())) {
                return result;
            } else {
                logger.warning("Invalid object found in the pool, destroy it: " + result.getObject());
                this.partitions[result.getPartition()].decreaseObject(result);
            }
        }
        throw new PoolInvalidObjectException();
    }

    @SuppressWarnings({"java:S112", "java:S2142"})
    private Poolable<T> getObject(boolean noTimeout) {
        if (shuttingDown) {
            throw new IllegalStateException("Your pool is shutting down");
        }
        int partition = (int) (Thread.currentThread().getId() % this.config.getPartitionSize());
        ObjectPoolPartition<T> subPool = this.partitions[partition];
        Poolable<T> freeObject;
        do { // loop to ensure: if T1 increases an object but T2 takes it, then T1 can poll and increase it again
            freeObject = subPool.getObjectQueue().poll();
            if (freeObject == null && subPool.increaseObjects(1) <= 0) { // full, have to wait
                freeObject = waitWhenSubPoolIsFull(noTimeout, subPool);
            }
        } while (freeObject == null);
        freeObject.setLastAccessTs(System.currentTimeMillis());
        return freeObject;
    }

    private Poolable<T> waitWhenSubPoolIsFull(boolean noTimeout, ObjectPoolPartition<T> subPool) {
        Poolable<T> freeObject;
        try {
            if (noTimeout) {
                freeObject = subPool.getObjectQueue().take();
            } else {
                freeObject = subPool.getObjectQueue().poll(config.getMaxWaitMilliseconds(), TimeUnit.MILLISECONDS);
                if (freeObject == null) {
                    throw new PoolExhaustedException();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e); // will never happen
        }
        return freeObject;
    }

    @SuppressWarnings({"java:S112", "java:S2142"})
    public void returnObject(Poolable<T> obj) {
        ObjectPoolPartition<T> subPool = this.partitions[obj.getPartition()];
        try {
            subPool.getObjectQueue().put(obj);
            if (logger.isLoggable(Level.FINE))
                logger.fine("return object: queue size:" + subPool.getObjectQueue().size() +
                    ", partition id:" + obj.getPartition());
        } catch (InterruptedException e) {
            throw new RuntimeException(e); // impossible for now, unless there is a bug, e,g. borrow once but return twice.
        }
    }

    public int getSize() {
        int size = 0;
        for (ObjectPoolPartition<T> subPool : partitions) {
            size += subPool.getTotalCount();
        }
        return size;
    }

    public synchronized int shutdown() throws InterruptedException {
        shuttingDown = true;
        int removed = 0;
        if (scavenger != null) {
            scavenger.interrupt();
            scavenger.join();
        }
        for (ObjectPoolPartition<T> partition : partitions) {
            removed += partition.shutdown();
        }
        return removed;
    }

    private class Scavenger extends Thread {

        @Override @SuppressWarnings({"java:S2142", "java:S108"})
        public void run() {
            int partition = 0;
            while (!ObjectPool.this.shuttingDown) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(config.getScavengeIntervalMilliseconds());
                    partition = ++partition % config.getPartitionSize();
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("scavenge sub pool " + partition);
                    }
                    partitions[partition].scavenge();
                } catch (InterruptedException ignored) {
                }
            }
        }

    }
}
