import com.haiwanwan.common.objectpool.ObjectFactory;
import com.haiwanwan.common.objectpool.ObjectPool;
import com.haiwanwan.common.objectpool.PoolConfig;
import com.haiwanwan.common.objectpool.Poolable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestShrink {


    @Test
    public void testShrink() throws InterruptedException {
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
        final ObjectPool pool = new ObjectPool(config, factory);

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
        new Thread() {
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
        }.start();
        Thread.sleep(1000);
        int removed = pool.shutdown(); // this will block 9 seconds
        assertEquals(4, removed);
    }
}