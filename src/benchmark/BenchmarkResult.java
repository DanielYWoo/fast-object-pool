public class BenchmarkResult {

    private final String poolName;
    private final long created;
    private final double errorRate;
    private final double avgThroughput;
    private final double avgLatency;
    private final int threads;
    private final int borrows;
    private final long loops;

    public BenchmarkResult(String poolName,
                           int threads, int borrows, long loops,
                           long created, double errorRate, double avgThroughput, double avgLatency) {
        this.poolName = poolName;
        this.threads = threads;
        this.borrows = borrows;
        this.loops = loops;
        this.created = created;
        this.errorRate = errorRate;
        this.avgThroughput = avgThroughput;
        this.avgLatency = avgLatency;
    }

    @Override
    public String toString() {
        return poolName +
                "," + threads +
                "," + borrows +
                "," + loops +
                "," + created +
                "," + plotLegend() +
                "," + errorRate +
                "," + avgThroughput;
    }

    private String plotLegend() {
        return poolName + "/" + threads;
    }
}
