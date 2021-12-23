package cn.danielw.fop;

import org.junit.Test;

import static java.lang.Math.abs;
import static org.junit.Assert.*;

public class PoolConfigTest {

    @Test
    public void testMaxWait() {
        PoolConfig c = new PoolConfig();
        assertEquals(5000, c.getMaxWaitMilliseconds()); // default value
        c.setMaxWaitMilliseconds(10000);
        assertEquals(10000, c.getMaxWaitMilliseconds());
        assertThrows(IllegalArgumentException.class, () -> c.setMaxWaitMilliseconds(0));
        assertThrows(IllegalArgumentException.class, () -> c.setMaxWaitMilliseconds(-10));
    }

    @Test
    public void testScavenge() {
        PoolConfig c = new PoolConfig();
        assertEquals(1000 * 60 * 2, c.getScavengeIntervalMilliseconds()); // default value
        c.setScavengeIntervalMilliseconds(0);
        assertEquals(0, c.getScavengeIntervalMilliseconds());
        c.setScavengeIntervalMilliseconds(10000);
        assertEquals(10000, c.getScavengeIntervalMilliseconds());
        assertThrows(IllegalArgumentException.class, () -> c.setScavengeIntervalMilliseconds(10));

        assertEquals(c.getScavengeRatio(), 0.5, 0.00000001);
        c.setScavengeRatio(0.85);
        assertEquals(c.getScavengeRatio(), 0.85, 0.00000001);
        c.setScavengeRatio(1);
        assertEquals(c.getScavengeRatio(), 1, 0.00000001);
        assertThrows(IllegalArgumentException.class, () -> c.setScavengeRatio(-0.1));
        assertThrows(IllegalArgumentException.class, () -> c.setScavengeRatio(0));
        assertThrows(IllegalArgumentException.class, () -> c.setScavengeRatio(1.1));
    }

    @Test
    public void testShutdown() {
        PoolConfig c = new PoolConfig();
        assertEquals(1000 * 30, c.getShutdownWaitMilliseconds());
        c.setShutdownWaitMilliseconds(1000 * 40);
        assertEquals(1000 * 40, c.getShutdownWaitMilliseconds());
        assertThrows(IllegalArgumentException.class, () -> c.setShutdownWaitMilliseconds(-1000));
    }

}
