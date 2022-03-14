package cn.danielw.fop;

/**
 * @author Daniel
 */
public interface ObjectFactory<T> extends ObjectFactoryRaw<T> {

    /**
     * @return the object to be created
     */
    @Override
    default Poolable<T> create(ObjectPool<T> pool, int partition) {
        return new Poolable<>(create(), pool, partition);
    }
    T create();

    @Override
    default void recycle(Poolable<T> poolable) {
        recycle(poolable.getObject());
    }
    void recycle(T t);

    @Override
    default void restore(Poolable<T> poolable) {
        restore(poolable.getObject());
    }
    void restore(T t);

    @Override
    default void destroy(Poolable<T> poolable) {
        destroy(poolable.getObject());
    }
    void destroy(T t);

    @Override
    default boolean validate(Poolable<T> poolable) {
        return validate(poolable.getObject());
    }
    boolean validate(T t);
}
