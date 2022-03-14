package cn.danielw.fop;

/**
 * @author Daniel
 */
public interface ObjectFactoryRaw<T> {

    /**
     * @return the object to be created
     */
    Poolable<T> create(ObjectPool<T> pool, int partition);

    void destroy(Poolable<T> poolable);

    boolean validate(Poolable<T> poolable);

}
