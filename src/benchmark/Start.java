import java.util.*;
import java.util.stream.Collectors;

public class Start {

    // key: threads
    // value: pool-name and throughput
    private static final Map<Integer, Map<String, Double>> throughputByThreads = new TreeMap<>();
    // key: threads
    // value: pool-name and error rate
    private static final Map<Integer, Map<String, Double>> errorRateByThreads = new TreeMap<>();

    public static void main(String[] args) throws Exception {
        int simulateBlockingMs = 0;
        System.out.println("-----------warm up------------");
//        testFOP(50, 1, 1000, simulateBlockingMs);
        testFOPDisruptor(50, 1, 1000, simulateBlockingMs);
        testStormpot(50, 1, 1000, simulateBlockingMs);
        testFurious(50, 1, 1000, simulateBlockingMs);
        testCommon(50, 1, 1000, simulateBlockingMs);

        System.out.println("-----------warm up done, start test ------------");

        System.out.println("----------- borrow 1 ------------");
        throughputByThreads.clear();
        testAll(1, simulateBlockingMs);
        printResult();

        System.out.println("----------- borrow 2 ------------");
        throughputByThreads.clear();
        testAll(2, simulateBlockingMs);
        printResult();

        System.exit(0);
    }

    private static void printResult() {
        List<String> poolNames = new ArrayList<>(throughputByThreads.values().stream().findFirst().get().keySet());
        System.out.println("throughput result:");
        System.out.println("threads," + poolNames);
        throughputByThreads.forEach((threads, value) -> {
            System.out.print(threads + ",");
            poolNames.forEach(name -> System.out.print(value.getOrDefault(name, 0D) + ","));
            System.out.println();
        });

        System.out.println("error rate result:");
        System.out.println("threads," + poolNames);
        errorRateByThreads.forEach((threads, value) -> {
            System.out.print(threads + ",");
            poolNames.forEach(name -> System.out.print(value.getOrDefault(name, 0D) + ","));
            System.out.println();
        });
    }

    private static void testAll(int borrows, int simulateBlockingMs) throws Exception {
//        System.out.println("-----------fast object pool (borrow " + borrows + " objects each time)------------");
//        testFOP(borrows);
        System.out.println("-----------fast object pool with disruptor (borrow " + borrows + " object each time)------------");
        testFOPDisruptor(borrows, simulateBlockingMs);

        System.out.println("-----------stormpot object pool (borrow " + borrows + " object each time)------------");
        testStormpot(borrows, simulateBlockingMs);

        System.out.println("-----------furious object pool (borrow " + borrows + " object each time)------------");
        testFurious(borrows, simulateBlockingMs);

        System.out.println("------------Apache commons pool (borrow " + borrows + " object each time)-----------");
        testCommon(borrows, simulateBlockingMs);
    }

    private static void testCommon(int borrows, int simulateBlockingMs) throws Exception {
        // too slow, so less loops
        testCommon(50,  borrows, 20000, simulateBlockingMs);
        testCommon(100, borrows, 10000, simulateBlockingMs);
//        testCommon(150, borrows, 9000, simulateBlockingMs);
        testCommon(200, borrows, 8000, simulateBlockingMs);
//        testCommon(250, borrows, 7000, simulateBlockingMs);
//        testCommon(300, borrows, 6000, simulateBlockingMs);
//        testCommon(350, borrows, 5000, simulateBlockingMs);
        testCommon(400, borrows, 4000, simulateBlockingMs);
//        testCommon(450, borrows, 3000, simulateBlockingMs);
//        testCommon(500, borrows, 2000, simulateBlockingMs);
//        testCommon(550, borrows, 1000, simulateBlockingMs);
        testCommon(600, borrows, 1000, simulateBlockingMs);
    }

    private static void testFurious(int borrows, int simulateBlockingMs) throws InterruptedException {
        if (borrows > 1) {
            System.out.println("Furious cannot set max wait time, so it will hang with deadlock if get more than 1 object concurrently");
            return;
        }
        testFurious(50,  borrows, 50000, simulateBlockingMs);
        testFurious(100, borrows, 50000, simulateBlockingMs);
//        testFurious(150, borrows, 50000, simulateBlockingMs);
        testFurious(200, borrows, 30000, simulateBlockingMs);
//        testFurious(250, borrows, 30000, simulateBlockingMs);
//        testFurious(300, borrows, 30000, simulateBlockingMs);
//        testFurious(350, borrows, 20000, simulateBlockingMs);
        testFurious(400, borrows, 20000, simulateBlockingMs);
//        testFurious(450, borrows, 20000, simulateBlockingMs);
//        testFurious(500, borrows, 10000, simulateBlockingMs);
//        testFurious(550, borrows, 10000, simulateBlockingMs);
        testFurious(600, borrows, 10000, simulateBlockingMs);
    }

    private static void testStormpot(int borrows, int simulateBlockingMs) throws InterruptedException {
        testStormpot(50,  borrows, 50000, simulateBlockingMs);
        testStormpot(100, borrows, 50000, simulateBlockingMs);
//        testStormpot(150, borrows, 50000);
        if (borrows > 1) {
            return; // this is too slow, skip it
        }
        testStormpot(200, borrows, 30000, simulateBlockingMs);
//        testStormpot(250, borrows, 30000);
//        testStormpot(300, borrows, 30000);
//        testStormpot(350, borrows, 20000);
        testStormpot(400, borrows, 20000, simulateBlockingMs);
//        testStormpot(450, borrows, 20000);
//        testStormpot(500, borrows, 10000);
//        testStormpot(550, borrows, 10000);
        testStormpot(600, borrows, 10000, simulateBlockingMs);
    }


    private static void testFOP(int borrowsPerLoop, int simulateBlockingMs) throws InterruptedException {
        testFOP(50,  borrowsPerLoop, 50000, simulateBlockingMs);
        testFOP(100, borrowsPerLoop, 50000, simulateBlockingMs);
        testFOP(150, borrowsPerLoop, 50000, simulateBlockingMs);
        testFOP(200, borrowsPerLoop, 30000, simulateBlockingMs);
        testFOP(250, borrowsPerLoop, 30000, simulateBlockingMs);
        testFOP(300, borrowsPerLoop, 30000, simulateBlockingMs);
        testFOP(350, borrowsPerLoop, 20000, simulateBlockingMs);
        testFOP(400, borrowsPerLoop, 20000, simulateBlockingMs);
        testFOP(450, borrowsPerLoop, 20000, simulateBlockingMs);
        testFOP(500, borrowsPerLoop, 10000, simulateBlockingMs);
        testFOP(550, borrowsPerLoop, 10000, simulateBlockingMs);
        testFOP(600, borrowsPerLoop, 10000, simulateBlockingMs);
    }

    private static void testFOPDisruptor(int borrows, int simulateBlockingMs) throws InterruptedException {
        testFOPDisruptor(50, borrows, 50000, simulateBlockingMs);
        testFOPDisruptor(100,borrows, 50000, simulateBlockingMs);
//        testFOPDisruptor(150,borrows, 50000, simulateBlockingMs);
        testFOPDisruptor(200,borrows, 30000, simulateBlockingMs);
//        testFOPDisruptor(250,borrows, 30000, simulateBlockingMs);
//        testFOPDisruptor(300,borrows, 30000, simulateBlockingMs);
//        testFOPDisruptor(350,borrows, 20000, simulateBlockingMs);
        testFOPDisruptor(400,borrows, 20000, simulateBlockingMs);
//        testFOPDisruptor(450,borrows, 20000, simulateBlockingMs);
//        testFOPDisruptor(500,borrows, 10000, simulateBlockingMs);
//        testFOPDisruptor(550,borrows, 10000, simulateBlockingMs);
        testFOPDisruptor(600,borrows, 10000, simulateBlockingMs);
    }

    private static void testFOP(int workerCount, int borrowsPerLoop, int loop, int simulateBlockingMs) throws InterruptedException {
        BenchmarkResult result = new BenchmarkFastObjectPool("fop-nod", workerCount, borrowsPerLoop, loop, simulateBlockingMs).testAndPrint();
        throughputByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getAvgThroughput());
        errorRateByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getErrorRate());
        cleanup();
    }

    private static void testFOPDisruptor(int workerCount, int borrowsPerLoop, int loop, int simulateBlockingMs) throws InterruptedException {
        BenchmarkResult result = new BenchmarkFastObjectPoolDisruptor(workerCount, borrowsPerLoop, loop, simulateBlockingMs).testAndPrint();
        throughputByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getAvgThroughput());
        errorRateByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getErrorRate());
        cleanup();
    }

    private static void testStormpot(int workerCount, int borrowsPerLoop, int loop, int simulateBlockingMs) throws InterruptedException {
        BenchmarkResult result = new BenchmarkStormpot(workerCount, borrowsPerLoop, loop, simulateBlockingMs).testAndPrint();
        throughputByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getAvgThroughput());
        errorRateByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getErrorRate());
        cleanup();
    }

    private static void testFurious(int workerCount, int borrowsPerLoop, int loop, int simulateBlockingMs) throws InterruptedException {
        BenchmarkResult result = new BenchmarkFurious(workerCount, borrowsPerLoop, loop, simulateBlockingMs).testAndPrint();
        throughputByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getAvgThroughput());
        errorRateByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getErrorRate());
        cleanup();
    }

    private static void testCommon(int workerCount, int borrowsPerLoop, int loop, int simulateBlockingMs) throws Exception {
        BenchmarkResult result = new BenchmarkCommons(workerCount, borrowsPerLoop, loop, simulateBlockingMs).testAndPrint();
        throughputByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getAvgThroughput());
        errorRateByThreads.computeIfAbsent(workerCount, k -> new TreeMap<>()).put(result.getPoolName(), result.getErrorRate());
        cleanup();
    }

    private static void cleanup() {
        try {
            System.out.println("cleaning up ...");
            Thread.sleep(1000L * 2);
            System.gc();
            Thread.sleep(1000L * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        System.out.println();
    }
}
