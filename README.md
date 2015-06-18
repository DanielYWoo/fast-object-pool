[![Build Status](https://travis-ci.org/DanielYWoo/fast-object-pool.svg?branch=master)](https://travis-ci.org/DanielYWoo/fast-object-pool)

fast-object-pool
================
FOP, a lightweight partitioned object pool, you can use it to pool expensive and non-thread-safe objects like thrift clients etc.

Why yet another object pool
--------------

FOP is implemented with partitions to avoid thread contention, the performance test shows it's much faster than Apache commons-pool. This project is not to replace Apache commons-pool, this project does not provide rich features like commons-pool, this project mainly aims on:
1). Zero dependency
2). High throughput with many concurrent requests
3). Less code so everybody can read it and understand it.


Configuration
-------------
First of all you need to create a FOP config:
```
        PoolConfig config = new PoolConfig();
        config.setPartitionSize(5);
        config.setMaxSize(10);
        config.setMinSize(5);
        config.setMaxIdleMilliseconds(60 * 1000 * 5);
```
The code above means the pool will have at least 5*5=25 objects, at most 5*10=50 objects, if an object has not been used over 5 minutes it could be removed.

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

Now you can create your FOP pool
```
ObjectPool pool = new ObjectPool(config, factory);
Poolable<StringBuilder> obj = null;
try {
    obj = pool.borrowObject();
    obj.getObject().append("x"); // do something
} finally {
    if (obj != null) {
        pool.returnObject(obj);
        // or obj.returnObject();
    }
}
```

Shut it down
```
pool.shutdown();
```

How it works
--------------
The pool will create multiple partitions, in most cases a thread always access a specified partition, so the more partitions you have, the less probability you run into thread contentions. Each partition has a blocking queue (JCL wait-free CAS/LLSC based) to hold poolable objects; to borrow an object, the first object in the queue will be removed; returning an object will append that object to the end of the queue. The idea is from ConcurrentHashMap's segments design and BoneCP connection pool's partition design.


How fast it is
--------------
The source contains a benchmark test, you can run it on your own machine. On my 2010-mid iMac, it's at least 50 times faster than commons-pool 1.x. As threads increase, commons-pool throughput drops quickly but FOP remains very good performance.
The figure belows shows that Commons-Pool 1.x is much slower than FOP, and when the worker threads goes up to 100, Commons-Pool throughput drops dramatically.

![](docs/benchmark.png?raw=true)

I believe Commons-Pool 2.x will be much faster since they rewriten everything. Update: I updated the benchmark with Commons-Pool 2.2, FOP is still 30 times faster than CP 2.2.

Maven dependency
---------------
To use this project, simply add this to your pom.xml
```
        <dependency>
            <groupId>com.haiwanwan.common</groupId>
            <artifactId>fast-object-pool</artifactId>
            <version>1.0.2</version>
        </dependency>
```
By default the debug messages are logged to JDK logger because one of the goals of this project is ZERO DEPENDENCY. However SLF4j is supported, checkout this for more details: http://www.slf4j.org/legacy.html#jul-to-slf4j

Apache commons-logging is not supported because: http://articles.qos.ch/thinkAgain.html
