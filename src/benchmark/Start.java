import java.util.ArrayList;
import java.util.List;

public class Start {

    private static List<String> csvLines = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        System.out.println("-----------warm up------------");
//        testFOP(50, 1, 1000);
        testFOPDisruptor(50, 1, 1000);
        testStormpot(50, 1, 1000);
        testFurious(50, 1, 1000);
        testCommon(50, 1, 1000);
        csvLines.clear();

        System.out.println("-----------warm up done, start test ------------");
//        testAll(1);
//        printResult(csvLines);
        testAll(2);
        printResult(csvLines);
        System.exit(0);
    }

    private static void printResult(List<String> csvLines) {
        System.out.println("name,threads,borrows,loops,pool size,legend,error rate,throughput");
        csvLines.forEach(System.out::println);
        System.out.println();
    }

    private static void testAll(int borrows) throws Exception {
//        System.out.println("-----------fast object pool (borrow " + borrows + " objects per time)------------");
//        testFOP(borrows);

        System.out.println("-----------fast object pool with disruptor (borrow " + borrows + " object per time)------------");
        testFOPDisruptor(borrows);

        System.out.println("-----------stormpot object pool (borrow " + borrows + " object per time)------------");
        testStormpot(borrows);

        System.out.println("-----------furious object pool (borrow " + borrows + " object per time)------------");
        testFurious(borrows);

        System.out.println("------------Apache commons pool (borrow " + borrows + " object per time)-----------");
        testCommon(borrows);

    }

    private static void testCommon(int borrows) throws Exception {
        // too slow, so less loops
        testCommon(50,  borrows, 20000);
        testCommon(100, borrows, 10000);
//        testCommon(150, borrows, 9000);
        testCommon(200, borrows, 8000);
//        testCommon(250, borrows, 7000);
//        testCommon(300, borrows, 6000);
//        testCommon(350, borrows, 5000);
        testCommon(400, borrows, 4000);
//        testCommon(450, borrows, 3000);
//        testCommon(500, borrows, 2000);
//        testCommon(550, borrows, 1000);
        testCommon(600, borrows, 1000);
    }

    private static void testFurious(int borrows) throws InterruptedException {
        if (borrows > 1) {
            System.out.println("Furious cannot set max wait time, so it will hang with deadlock if get more than 1 object concurrently");
            return;
        }
        testFurious(50,  borrows, 50000);
        testFurious(100, borrows, 50000);
//        testFurious(150, borrows, 50000);
        testFurious(200, borrows, 30000);
//        testFurious(250, borrows, 30000);
//        testFurious(300, borrows, 30000);
//        testFurious(350, borrows, 20000);
        testFurious(400, borrows, 20000);
//        testFurious(450, borrows, 20000);
//        testFurious(500, borrows, 10000);
//        testFurious(550, borrows, 10000);
        testFurious(600, borrows, 10000);
    }

    private static void testStormpot(int borrows) throws InterruptedException {
        testStormpot(50,  borrows, 50000);
        testStormpot(100, borrows, 50000);
//        testStormpot(150, borrows, 50000);
        if (borrows > 1) {
            return; // this is too slow, skip it
        }
        testStormpot(200, borrows, 30000);
//        testStormpot(250, borrows, 30000);
//        testStormpot(300, borrows, 30000);
//        testStormpot(350, borrows, 20000);
        testStormpot(400, borrows, 20000);
//        testStormpot(450, borrows, 20000);
//        testStormpot(500, borrows, 10000);
//        testStormpot(550, borrows, 10000);
        testStormpot(600, borrows, 10000);
    }


    private static void testFOP(int borrowsPerLoop) throws InterruptedException {
        testFOP(50,  borrowsPerLoop, 50000);
        testFOP(100, borrowsPerLoop, 50000);
        testFOP(150, borrowsPerLoop, 50000);
        testFOP(200, borrowsPerLoop, 30000);
        testFOP(250, borrowsPerLoop, 30000);
        testFOP(300, borrowsPerLoop, 30000);
        testFOP(350, borrowsPerLoop, 20000);
        testFOP(400, borrowsPerLoop, 20000);
        testFOP(450, borrowsPerLoop, 20000);
        testFOP(500, borrowsPerLoop, 10000);
        testFOP(550, borrowsPerLoop, 10000);
        testFOP(600, borrowsPerLoop, 10000);
    }

    private static void testFOPDisruptor(int borrows) throws InterruptedException {
        testFOPDisruptor(50, borrows, 50000);
        testFOPDisruptor(100,borrows, 50000);
//        testFOPDisruptor(150,borrows, 50000);
        testFOPDisruptor(200,borrows, 30000);
//        testFOPDisruptor(250,borrows, 30000);
//        testFOPDisruptor(300,borrows, 30000);
//        testFOPDisruptor(350,borrows, 20000);
        testFOPDisruptor(400,borrows, 20000);
//        testFOPDisruptor(450,borrows, 20000);
//        testFOPDisruptor(500,borrows, 10000);
//        testFOPDisruptor(550,borrows, 10000);
        testFOPDisruptor(600,borrows, 10000);
    }

    private static void testFOP(int workerCount, int borrowsPerLoop, int loop) throws InterruptedException {
        BenchmarkResult result = new BenchmarkFastObjectPool("fop-nod", workerCount, borrowsPerLoop, loop).testAndPrint();
        csvLines.add(result.toString());
        cleanup();
    }

    private static void testFOPDisruptor(int workerCount, int borrowsPerLoop, int loop) throws InterruptedException {
        BenchmarkResult result = new BenchmarkFastObjectPoolDisruptor(workerCount, borrowsPerLoop, loop).testAndPrint();
        csvLines.add(result.toString());
        cleanup();
    }

    private static void testStormpot(int workerCount, int borrowsPerLoop, int loop) throws InterruptedException {
        BenchmarkResult result = new BenchmarkStormpot(workerCount, borrowsPerLoop, loop).testAndPrint();
        csvLines.add(result.toString());
        cleanup();
    }

    private static void testFurious(int workerCount, int borrowsPerLoop, int loop) throws InterruptedException {
        BenchmarkResult result = new BenchmarkFurious(workerCount, borrowsPerLoop, loop).testAndPrint();
        csvLines.add(result.toString());
        cleanup();
    }

    private static void testCommon(int workerCount, int borrowsPerLoop, int loop) throws Exception {
        BenchmarkResult result = new BenchmarkCommons(workerCount, borrowsPerLoop, loop).testAndPrint();
        csvLines.add(result.toString());
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
        }
        System.out.println();
    }
}
