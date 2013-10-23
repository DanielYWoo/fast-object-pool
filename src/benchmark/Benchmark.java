/**
 * @author Daniel
 */
public class Benchmark {

    public static void main(String[] args) throws Exception {

        System.out.println("-----------warm up------------");
        new BenchmarkFastObjectPool(50,  1000);
        new BenchmarkCommons(50,  1000);
        new BenchmarkNoPool(50,  1000);

        System.out.println("-----------no pool------------");
        new BenchmarkNoPool(50,  1000);
        new BenchmarkNoPool(100,  1000);

        System.out.println("-----------fast object pool------------");
        new BenchmarkFastObjectPool(50,  500000);
        new BenchmarkFastObjectPool(100, 500000);
        new BenchmarkFastObjectPool(150, 300000);
        new BenchmarkFastObjectPool(200, 300000);
        new BenchmarkFastObjectPool(250, 100000);
        new BenchmarkFastObjectPool(300, 100000);
        new BenchmarkFastObjectPool(350, 50000);
        new BenchmarkFastObjectPool(400, 50000);
        new BenchmarkFastObjectPool(450, 20000);
        new BenchmarkFastObjectPool(500, 20000);
        new BenchmarkFastObjectPool(550, 10000);
        new BenchmarkFastObjectPool(600, 10000);

        System.out.println("------------Apache commons pool-----------");
        //new BenchmarkCommons(50,  500000);
        new BenchmarkCommons(100, 10000);
        new BenchmarkCommons(150, 300000);
        new BenchmarkCommons(200, 300000);
        new BenchmarkCommons(250, 100000);
        new BenchmarkCommons(300, 100000);
        new BenchmarkCommons(350, 50000);
        new BenchmarkCommons(400, 50000);
        new BenchmarkCommons(450, 20000);
        new BenchmarkCommons(500, 20000);
        new BenchmarkCommons(550, 10000);
        new BenchmarkCommons(600, 10000);

    }
}
