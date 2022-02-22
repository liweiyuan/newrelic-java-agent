package com.newrelic.agent.stats;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResponseTimeStatsImplMicroBenchmarks {

    public static void main(String[] args) {
        System.out.println("-----------------------------------------------------------------");
        ResponseTimeStats base = new ResponseTimeStatsImpl();
        printResultsFor(base, "Single Threaded Base", Executors.newSingleThreadExecutor());
        System.out.println("-----------------------------------------------------------------");
        ResponseTimeStats currentNotThreadSafe = new ResponseTimeStatsImpl();
        printResultsFor(currentNotThreadSafe, "Current Implementation (Warmup)");
        printResultsFor(currentNotThreadSafe, "Current Implementation (Benchmark #1)");
        printResultsFor(currentNotThreadSafe, "Current Implementation (Benchmark #2)");
        printResultsFor(currentNotThreadSafe, "Current Implementation (Benchmark #3)");
        printResultsFor(currentNotThreadSafe, "Current Implementation (Benchmark #4)");
        printResultsFor(currentNotThreadSafe, "Current Implementation (Benchmark #5)");
        printResultsFor(currentNotThreadSafe, "Current Implementation (Benchmark #6)");
        System.out.println("-----------------------------------------------------------------");
        ResponseTimeStats atomicImpl = new ResponseTimeStatsAtomicImpl();
        printResultsFor(atomicImpl, "Atomic Implementation (Warmup)");
        printResultsFor(atomicImpl, "Atomic Implementation (Benchmark #1)");
        printResultsFor(atomicImpl, "Atomic Implementation (Benchmark #2)");
        printResultsFor(atomicImpl, "Atomic Implementation (Benchmark #3)");
        printResultsFor(atomicImpl, "Atomic Implementation (Benchmark #4)");
        printResultsFor(atomicImpl, "Atomic Implementation (Benchmark #5)");
        printResultsFor(atomicImpl, "Atomic Implementation (Benchmark #6)");
        System.out.println("-----------------------------------------------------------------");
        ResponseTimeStats volatileImpl = new ResponseTimeStatsVolatileImpl();
        printResultsFor(volatileImpl, "Volatile Implementation (Warmup)");
        printResultsFor(volatileImpl, "Volatile Implementation (Benchmark #1)");
        printResultsFor(volatileImpl, "Volatile Implementation (Benchmark #2)");
        printResultsFor(volatileImpl, "Volatile Implementation (Benchmark #3)");
        printResultsFor(volatileImpl, "Volatile Implementation (Benchmark #4)");
        printResultsFor(volatileImpl, "Volatile Implementation (Benchmark #5)");
        printResultsFor(volatileImpl, "Volatile Implementation (Benchmark #6)");
        System.out.println("-----------------------------------------------------------------");
    }

    private static void printResultsFor(ResponseTimeStats responseTimeStats, String header) {
        printResultsFor(responseTimeStats, header, Executors.newFixedThreadPool(10));
    }

    private static void printResultsFor(ResponseTimeStats responseTimeStats, String header,
                                        ExecutorService executorService) {
        System.out.println("*** " + header + " ***");
        System.out.println(doEet(responseTimeStats, executorService) + "ms");
        System.out.println(responseTimeStats);
        responseTimeStats.reset();
    }

    public static long doEet(ResponseTimeStats responseTimeStats, ExecutorService executor) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 100_000; j++) {
                    responseTimeStats.recordResponseTimeInNanos(1);
                }
            });
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        long end = System.currentTimeMillis();
        return end - start;
    }
}
