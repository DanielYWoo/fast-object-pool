fast-object-pool
================
FOP, a lightweight high performance object pool optimized for concurrent accesses, you can use it to pool expensive and non-thread-safe objects like thrift clients etc.

Github repo: [https://github.com/pro100kryto/fast-object-pool](https://github.com/DanielYWoo/fast-object-pool)

Why yet another object pool
--------------

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
ObjectFactory<ByteBuffer> factory = new ObjectFactory<>() {
    @Override public ByteBuffer create() {
        return ByteBuffer.allocate(1024); // create your object here
    }
    @override public void recycle(ByteBuffer o) {
        o.clear(); // clean up before return object to pool
    }
    @override public void restore(ByteBuffer o) {
        o.put(123); // prepare object after borrow
    }
    @Override public void destroy(ByteBuffer o) {
        // release resources
    }
    @Override public boolean validate(ByteBuffer o) {
        return o.position() == 0; // validate your object here
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

