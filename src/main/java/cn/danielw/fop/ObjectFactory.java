package cn.danielw.fop;

/**
 * @author Daniel
 */
public interface ObjectFactory<T> {

    /**
     * @return the object to be created
     */
    T create();

    /**
     * destroy the object gracefully.
     */
    void destroy(T t);

    /**
     * validate the object before returning to the consumer. Note, the validation must be fast
     */
    boolean validate(T t);

}
