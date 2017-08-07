import stormpot.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniel
 */
public class BenchmarkStormpot extends Benchmark {

    static class MyPoolable implements stormpot.Poolable {

        private StringBuilder test;

        public MyPoolable() {
            test = new StringBuilder();
        }

        public StringBuilder getTest() {
            return test;
        }

        @Override
        public void release() {

        }
    }


    public BenchmarkStormpot(int workerCount, int loop) throws InterruptedException {
        super(workerCount, loop);

        Config<MyPoolable> config = new Config<>().setAllocator(new Allocator<MyPoolable>() {
            @Override
            public MyPoolable allocate(Slot slot) throws Exception {
                return new MyPoolable();
            }

            @Override
            public void deallocate(MyPoolable x) throws Exception {

            }
        }).setSize(50);
        /*
        config.setExpiration(new Expiration<MyPoolable>() {
            @Override
            public boolean hasExpired(SlotInfo<? extends MyPoolable> slotInfo) throws Exception {
                // how to support max idle?
                return slotInfo.getAgeMillis() > 60 * 10000 * 5;
            }
        });
        */
        Pool<MyPoolable> pool = new BlazePool<>(config);
        Worker[] workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this, i, latch, loop, pool);
        }
        testAndPrint(workers);
    }

    private static class Worker extends BaseWorker {

        private static final Timeout TIMEOUT = new Timeout(1, TimeUnit.HOURS);
        private final Pool<MyPoolable> pool;

        public Worker(Benchmark benchmark, int id, CountDownLatch latch, int loop, Pool<MyPoolable> pool) {
            super(benchmark, id, latch, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
            MyPoolable obj = null;
            try {
                obj = pool.claim(TIMEOUT);
                // todo: check NPE
                obj.getTest().append("x");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (obj != null) {
                    obj.release();
                }
            }
        }
    }
}
