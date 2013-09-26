package com.haiwanwan.common.objectpool;

/**
 * @author Daniel
 */
public interface ObjectFactory<T> {

    public T create();

    public void destroy(T t);

    public boolean validate(T t);

}
