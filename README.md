[![Build Status](https://travis-ci.org/DanielYWoo/fast-object-pool.svg?branch=master)](https://travis-ci.org/DanielYWoo/fast-object-pool)

fast-object-pool
================
FOP, a lightweight partitioned object pool, you can use it to pool expensive and non-thread-safe objects like thrift clients etc.

Github repo: https://github.com/DanielYWoo/fast-object-pool

Site page: https://danielw.cn/fast-object-pool/

Why yet another object pool
--------------

FOP is implemented with partitions to avoid thread contention, the performance test shows it's much faster than Apache commons-pool. This project is not to replace Apache commons-pool, this project does not provide rich features like commons-pool, this project mainly aims on:
1. Zero dependency
2. High throughput with many concurrent requests
3. Less code so everybody can read it and understand it.

Configuration
-------------
First of all you need to create a FOP config:


```java
PoolConfig config = new PoolConfig();
config.setPartitionSize(5);
config.setMaxSize(10);
config.setMinSize(5);
config.setMaxIdleMilliseconds(60 * 1000 * 5);
```


The code above means the pool will have at least 5x5=25 objects, at most 5x10=50 objects, if an object has not been used over 5 minutes it could be removed.

Then define how objects will be created and destroyed with ObjectFactory


```java
ObjectFactory<StringBuilder> factory = new ObjectFactory<>() {
    @Override public StringBuilder create() {
        return new StringBuilder(); // create your object here
    }
    @Override public void destroy(StringBuilder o) {
        // clean up and release resources
    }
    @Override public boolean validate(StringBuilder o) {
        return true; // validate your object here
    }
};
```


Now you can create your FOP pool and just use it


```java
ObjectPool pool = new ObjectPool(config, factory);
try (Poolable<Connection> obj = pool.borrowObject()) {
    obj.getObject().sendPackets(somePackets);
}
```

If you want best performance, you need add disruptor queue to your dependency and use DisruptorObjectPool. 
Note, the DisruptorPool has not been fully tested in production yet.

Shut it down


```java
pool.shutdown();

```

How it works
--------------
The pool will create multiple partitions, in most cases a thread always access a specified partition, so the more partitions you have, the less probability you run into thread contentions. Each partition has a blocking queue to hold poolable objects; to borrow an object, the first object in the queue will be removed; returning an object will append that object to the end of the queue. The idea is from ConcurrentHashMap's segments design and BoneCP connection pool's partition design. This project started since 2013 and has been deployed to many projects without any problem.

How fast it is
--------------
The source contains a benchmark test, you can run it on your own machine. On my 2016 Macbook Pro, it's 20-40 times faster than commons-pool 2.2.
![](docs/benchmark.png?raw=true)

You can see more detailed comparison including error rate below. From the figure below you can see 
stormpot is the fastest, if you only borrow one object at a time per thread, you can use stormpot. 
Apache commons pool is not recommended.
![](docs/b1-throughput.png?raw=true)
![](docs/b1-error-rate.png?raw=true)

If we borrow two objects in each thread then return them to the pool, things are getting more interesting.
Stormpot becomes very slow at 100 threads, so I cannot continue to test with more threads. Furious simply does not work, 
because when you have more than 256 threads contend on 256 objects, some threads will hang (deadlock). Both FOP and Stormpot 
provides timeout support, FOP throws an exception, Stormpot returns null, so we can mark it as failed (show in the error rate plot). 
But I cannot find a way to set timeout in Furious, so I totally exclude it from the diagram below. If you know how please let me know.
![](docs/b2-throughput.png?raw=true)
![](docs/b2-error-rate.png?raw=true)

Maven dependency
---------------
To use this project, simply add this to your pom.xml


```xml
<dependency>
    <groupId>cn.danielw</groupId>
    <artifactId>fast-object-pool</artifactId>
    <version>2.1.1</version>
</dependency>
```

If you want disruptor object pool, add this optional dependency

JDK 8,9,10
```xml
<dependency>
    <groupId>com.conversantmedia</groupId>
    <artifactId>disruptor</artifactId>
    <version>1.2.15</version>
</dependency>
```
JDK 11+
```xml
<dependency>
    <groupId>com.conversantmedia</groupId>
    <artifactId>disruptor</artifactId>
    <version>1.2.19</version>
</dependency>
```

JDK 8/11 is required. By default the debug messages are logged to JDK logger because one of the goals of this project is ZERO DEPENDENCY. However we have two optional dependencies, disruptor and SLF4j.

