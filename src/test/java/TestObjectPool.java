import com.haiwanwan.common.objectpool.ObjectFactory;
import com.haiwanwan.common.objectpool.ObjectPool;
import com.haiwanwan.common.objectpool.PoolConfig;
import com.haiwanwan.common.objectpool.Poolable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class TestObjectPool {

    private ObjectPool<StringBuilder> pool;

    @Before
    public void init() {
        Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
        Logger.getLogger("").setLevel(Level.ALL);
        PoolConfig config = new PoolConfig();
        config.setPartitionSize(2);
        config.setMaxSize(20);
        config.setMinSize(2);
        config.setMaxIdleMilliseconds(5000);
        config.setScavengeIntervalMilliseconds(5000);

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
        pool = new ObjectPool(config, factory);
    }

    @Test
    public void testSimple() throws InterruptedException {
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

        Thread.sleep(20000);
        assertEquals(4, pool.getSize());
        System.out.println("scavenged, pool size=" + pool.getSize());

        // test return after shutdown
        Thread testThread = new Thread() {
            @Override
            public void run() {
                Poolable<StringBuilder> obj = pool.borrowObject();
                try {
                    System.out.println("pool size:" + pool.getSize());
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
                pool.returnObject(obj);
                System.out.println("pool size:" + pool.getSize());
            }
        };
        testThread.start();
        testThread.join();
        int removed = pool.shutdown(); // this will block 9 seconds
        assertEquals(4, removed);
        System.out.println("All done");
    }
}