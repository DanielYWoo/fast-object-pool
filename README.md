fast-object-pool
================

A lightweight partitioned object pool, you can use it to pool expensive objects like jdbc connections, thrift clients etc.

Why yet another object pool
--------------

The pool is implemented with partitions to avoid thread contention, the performance test shows it's much faster than Apache commons-pool. This project is not to replace Apache commons-pool, this project does not provide rich features like commons-pool, this project mainly aims on:
1). Zero dependency
2). High throughput with many concurrent requests
3). Less code so everybody can read it and understand it


Configuration
-------------
First of all you need to create a config:
```
        PoolConfig config = new PoolConfig();
        config.setPartitionSize(5);
        config.setMaxSize(10);
        config.setMinSize(5);
        config.setMaxIdleMilliseconds(60 * 1000 * 5);

```

Then define how objects will be created and destroyed with ObjectFactory
```
        ObjectFactory<StringBuilder> factory = new ObjectFactory<StringBuilder>() {
            @Override
            public StringBuilder create() {
                return new StringBuilder();
            }
            @Override
            public void destroy(StringBuilder o) {
            }
            @Override
            public boolean validate(StringBuilder o) {
                return true;
            }
        };

```

Now you can create your pool
```
ObjectPool pool = new ObjectPool(config, factory);
Poolable<StringBuilder> obj = null;
try {
    obj = pool.borrowObject();
    obj.getObject().append("x");
} finally {
    if (obj != null) {
        pool.returnObject(obj);
    }
}

```
