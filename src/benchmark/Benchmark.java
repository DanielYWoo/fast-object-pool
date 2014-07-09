/**
 * @author Daniel
 */
public class Benchmark {

    public static void main(String[] args) throws Exception {

        System.out.println("-----------warm up------------");
        new BenchmarkFastObjectPool(50,  1000);
        new BenchmarkCommons(50,  1000);

        System.out.println("-----------fast object pool------------");
        new BenchmarkFastObjectPool(50,  50000);
        new BenchmarkFastObjectPool(100, 50000);
        new BenchmarkFastObjectPool(150, 50000);
        new BenchmarkFastObjectPool(200, 30000);
        new BenchmarkFastObjectPool(250, 30000);
        new BenchmarkFastObjectPool(300, 30000);
        new BenchmarkFastObjectPool(350, 20000);
        new BenchmarkFastObjectPool(400, 20000);
        new BenchmarkFastObjectPool(450, 20000);
        new BenchmarkFastObjectPool(500, 10000);
        new BenchmarkFastObjectPool(550, 10000);
        new BenchmarkFastObjectPool(600, 10000);

        System.out.println("------------Apache commons pool-----------");
        // too slow, so less loops
        new BenchmarkCommons(50, 20000);
        new BenchmarkCommons(100, 10000);
        new BenchmarkCommons(150, 90000);
        new BenchmarkCommons(200, 8000);
        new BenchmarkCommons(250, 7000);
        new BenchmarkCommons(300, 6000);
        new BenchmarkCommons(350, 5000);
        new BenchmarkCommons(400, 4000);
        new BenchmarkCommons(450, 3000);
        new BenchmarkCommons(500, 2000);
        new BenchmarkCommons(550, 1000);
        new BenchmarkCommons(600, 1000);

    }
}
