import cn.danielw.fop.ObjectFactory;
import cn.danielw.fop.ObjectPool;
import cn.danielw.fop.PoolConfig;
import cn.danielw.fop.Poolable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class TestObjectPool {


    public ObjectPool<StringBuilder> init(double scavengeRatio) {
        Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
        Logger.getLogger("").setLevel(Level.ALL);
        PoolConfig config = new PoolConfig();
        config.setPartitionsCount(2).setMinPartitionSize(2).setMaxPartitionSize(20).setMaxIdleMilliseconds(5000).
                setMaxWaitMilliseconds(100).setScavengeIntervalMilliseconds(5000).setScavengeRatio(scavengeRatio);

        ObjectFactory<StringBuilder> factory = new ObjectFactory<StringBuilder>() {
            @Override
            public StringBuilder create() {
                return new StringBuilder();
            }

            @Override
            public void recycle(StringBuilder stringBuilder) {
            }

            @Override
            public void restore(StringBuilder stringBuilder) {
            }

            @Override
            public void destroy(StringBuilder o) {
            }

            @Override
            public boolean validate(StringBuilder o) {
                return true;
            }
        };
        return new ObjectPool<>(config, factory);
    }

    @Test
    public void testSimple() {
        ObjectPool<StringBuilder> pool = init(1.0);
        for (int i = 0; i < 100; i++) {
            try (Poolable<StringBuilder> obj = pool.borrowObject()) {
                obj.getObject().append("x");
            }
        }
        System.out.println("pool size:" + pool.getSize());
        assertEquals(4, pool.getSize());
    }

    @Test
    public void testShrink() throws InterruptedException {
        final ObjectPool<StringBuilder> pool = init(1.0);
        List<Poolable<StringBuilder>> borrowed = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            System.out.println("test borrow");
            Poolable<StringBuilder> obj = pool.borrowObject();
            obj.getObject().append("x");
            borrowed.add(obj);
        }
        System.out.println("pool size:" + pool.getSize());

        for (Poolable<StringBuilder> obj : borrowed) {
            System.out.println("test return");
            pool.returnObject(obj);
        }
        assertEquals(12, pool.getSize());
        System.out.println("pool size:" + pool.getSize());

        Thread.sleep(20000); //NOSONAR
        assertEquals(4, pool.getSize());
        System.out.println("scavenged, pool size=" + pool.getSize());

        // test return after shutdown
        Thread testThread = new Thread(() -> {
            Poolable<StringBuilder> obj = pool.borrowObject();
            System.out.println("pool size:" + pool.getSize());
            try { Thread.sleep(10000); } catch (InterruptedException ignored) { }
            pool.returnObject(obj);
            System.out.println("pool size:" + pool.getSize());
        });
        testThread.start();
        testThread.join();
        int removed = pool.shutdown(); // this will block 9 seconds
        assertEquals(4, removed);
        System.out.println("All done");
    }

}