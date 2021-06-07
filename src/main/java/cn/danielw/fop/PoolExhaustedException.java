package cn.danielw.fop;

/**
 * When the pool has been exhausted, <code>getObject(false)</code> will throw this exception
 * if no object is available within <code>PoolConfig.maxWaitMilliseconds</code>.
 */
public class PoolExhaustedException extends RuntimeException {
}
