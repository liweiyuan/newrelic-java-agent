package com.newrelic.agent.stats;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResponseTimeStatsImplTest {

    public static void main(String[] args) {
        ResponseTimeStats currentNotThreadSafe = new ResponseTimeStatsImpl();
        printResultsFor(currentNotThreadSafe, "Current Implementation (Warmup)");
        printResultsFor(currentNotThreadSafe, "Current Implementation (Benchmark #1)");
        printResultsFor(currentNotThreadSafe, "Current Implementation (Benchmark #2)");
        printResultsFor(currentNotThreadSafe, "Current Implementation (Benchmark #3)");
        //printResultsFor(new ResponseTimeStatsAtomicImpl(), "Atomic Implementation");
    }

    private static void printResultsFor(ResponseTimeStats responseTimeStats, String header) {
        System.out.println("*** " + header + " ***");
        System.out.println(doEet(responseTimeStats) + "ms");
        System.out.println(responseTimeStats);
        responseTimeStats.reset();
    }

    public static long doEet(ResponseTimeStats responseTimeStats) {
        ExecutorService multithreadedExecutor = Executors.newFixedThreadPool(10);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            multithreadedExecutor.submit(() -> {
                for (int j = 0; j < 100_000; j++) {
                    responseTimeStats.recordResponseTimeInNanos(1);
                }
            });
        }
        multithreadedExecutor.shutdown();
        try {
            if (!multithreadedExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                multithreadedExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            multithreadedExecutor.shutdownNow();
        }
        long end = System.currentTimeMillis();
        return end - start;
    }
}
