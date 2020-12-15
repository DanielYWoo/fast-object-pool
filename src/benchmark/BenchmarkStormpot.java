import stormpot.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniel
 */
public class BenchmarkStormpot extends Benchmark {

    private static final Set<Slot> slots = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static class MyPoolable implements stormpot.Poolable {

        private final Slot slot;
        private final StringBuilder test;

        MyPoolable(Slot slot) {
            test = new StringBuilder();
            this.slot = slot;
            slots.add(slot);
        }

        StringBuilder getTest() {
            return test;
        }

        @Override
        public void release() {
            slot.release(this);
        }
    }


    BenchmarkStormpot(int workerCount, int borrows, int loop) throws InterruptedException {
        super("Stormpot", workerCount, borrows, loop);

        Config<MyPoolable> config = new Config<>().setAllocator(new Allocator<MyPoolable>() {
            @Override
            public MyPoolable allocate(Slot slot) throws Exception {
                created.incrementAndGet();
                return new MyPoolable(slot);
            }

            @Override
            public void deallocate(MyPoolable x) throws Exception {

            }
        }).setSize(256);
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
        workers = new Worker[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this, i, borrows, loop, pool);
        }
        System.out.println("slots:" + slots.size());
    }

    private static class Worker extends BaseWorker {

        private static final Timeout TIMEOUT = new Timeout(10, TimeUnit.MILLISECONDS);
        private final Pool<MyPoolable> pool;

        Worker(Benchmark benchmark, int id, int borrows, int loop, Pool<MyPoolable> pool) {
            super(benchmark, id, borrows, loop);
            this.pool = pool;
        }

        @Override public void doSomething() {
            List<MyPoolable> list = new ArrayList<>();
            try {
                for (int i = 0; i < borrowsPerLoop; i++) {
                    MyPoolable obj = pool.claim(TIMEOUT);
                    obj.getTest().append("x");
                    list.add(obj);
                }
            } catch (Exception e) {
                err++;
            } finally {
                list.forEach(o -> {
                    try {
                        o.release();
                    } catch (Exception e) {
                        err++;
                    }
                });
            }
        }
    }
}
