[![Build Status](https://travis-ci.com/DanielYWoo/fast-object-pool.svg?branch=master)](https://travis-ci.com/github/DanielYWoo/fast-object-pool)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=DanielYWoo_fast-object-pool&metric=alert_status)](https://sonarcloud.io/dashboard?id=DanielYWoo_fast-object-pool)
[中文文档](/README_cn.md)

fast-object-pool
================
FOP, a lightweight high performance object pool optimized for concurrent accesses, you can use it to pool expensive and non-thread-safe objects like thrift clients etc.

Github repo: [https://github.com/DanielYWoo/fast-object-pool](https://github.com/DanielYWoo/fast-object-pool)

Site page: [https://danielw.cn/fast-object-pool/](https://danielw.cn/fast-object-pool/)

Why yet another object pool
--------------

FOP is implemented with partitions to avoid thread contention, the performance test shows it's much faster than Apache commons-pool. 
This project is not to replace Apache commons-pool, this project does not provide rich features like commons-pool, this project mainly aims on:
1. Zero dependency (the only optional dependency is disrutpor)
2. High throughput with many concurrent requests/threads
3. Less code so everybody can read it and understand it.

Configuration
-------------
First of all you need to create a FOP config:


```java
PoolConfig config = new PoolConfig();
config.setPartitionsCount(5);
config.setMaxPartitionSize(10);
config.setMinPartitionSize(5);
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

Shut it down


```java
pool.shutdown();

```

If you want best performance, you can optionally add disruptor to your dependency, and use DisruptorObjectPool instead of ObjectPool. (recommended)

For JDK 11+, use the dependency below.
```
Maven:
<dependency>
    <groupId>com.conversantmedia</groupId>
    <artifactId>disruptor</artifactId>
    <version>1.2.19</version>
</dependency>

Gradle:
implementation 'com.conversantmedia:disruptor:1.2.19'
```

Older JDKs like 8/9/10 can also use disruptor but with an older version 1.2.15.
```
Maven:
<dependency>
    <groupId>com.conversantmedia</groupId>
    <artifactId>disruptor</artifactId>
    <version>1.2.15</version>
</dependency>

Gradle:
implementation 'com.conversantmedia:disruptor:1.2.15'
```

Logging
--------------
One of the design goals of FOP is zero dependency, so we use JDK logger by default. If you use slf4j, you can optionally add jul-to-slf4j to your dependency to bridge the JDK logger to slf4j. 

How it works
--------------
The pool will create multiple partitions, in most cases a thread always access a specified partition, 
so the more partitions you have, the less probability you run into thread contentions. Each partition has a 
blocking queue to hold poolable objects; to borrow an object, the first object in the queue will be removed; 
returning an object will append that object to the end of the queue. The idea is from ConcurrentHashMap's segments 
design and BoneCP connection pool's partition design. This project started since 2013 and has been deployed to many projects without any problems.

How fast it is
--------------
The source contains a benchmark test, you can run it on your own machine. On my Macbook Pro (8-cores i9), it's 50 times faster than commons-pool 2.2. 

Test Case 1: we have a pool of 256 objects, use 50/100/400/600 threads to borrow one object each time in each thread, then return to the pool.

The x-axis in the diagram below is the number of threads, you can see Stormpot provides the best throughput. FOP is closely behind Stormpot. Apache common pool is very slow, Furious is slightly faster than it. 
You see the throughput drops after 200 threads because there are only 256 objects, so there will be data race and timeout with more threads.
![](docs/b1-throughput.png?raw=true)
When you have 600 threads contending 256 objects, Apache common pool reaches over 85% error rate (probably because returning is too slow), basically it cannot be used in high concurrency.
![](docs/b1-error-rate.png?raw=true)

The diagram above if you only borrow one object at a time per thread, if you need to borrow two objects in a thread, things are getting more interesting.

Test Case 2: we have a pool of 256 objects, use 50/100/400/600 threads to borrow two objects each time each thread, then return to the pool.

In this case, we could have 600x2=1200 objects required concurrently but only 256 is available, so there will be contention and timeout error. 
FOP keeps error rate steadily but stormpot starts to see 7% error rate with 100 threads. (I cannot test stormpot with 600 threads because it's too slow) 
Furious seems not working because I cannot find a way to set timeout, without timeout I see circular deadlock in Furious, 
so I exclude Furious from the diagram below . Both FOP and Stormpot provides timeout configuration, in the test I set timeout to 10ms. 
FOP throws an exception, Stormpot returns null, so we can mark it as failed (show in the error rate plot). 
Again, Apache common pool reaches almost 80% error rate, basically not working.
![](docs/b2-error-rate.png?raw=true)

The throughput of FOP is also much better than other pools. Interestingly, in this case, Apache common pool is slightly faster than Stormpot. 
I don't know why Stormpot degrade so fast with two borrows in one thread. If you know how to optimize Stormpot configuration for this case please let me know.
![](docs/b2-throughput.png?raw=true)

So, in short, if you can ensure borrow at most one object in each thread, Stormpot is the best choice. If you cannot ensure that, use FOP which is more consistent in all scenarios.

