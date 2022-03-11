package cn.danielw.fop;

/**
 * @author Daniel
 */
public interface ObjectFactory<T> {

    /**
     * @return the object to be created
     */
    default Poolable<T> create(ObjectPool<T> pool, int partition) {
        return new Poolable<>(create(), pool, partition);
    }
    T create();

    default void destroy(Poolable<T> poolable) {
        destroy(poolable.getObject());
    }
    void destroy(T t);

    default boolean validate(Poolable<T> poolable) {
        return validate(poolable.getObject());
    }
    boolean validate(T t);

}
