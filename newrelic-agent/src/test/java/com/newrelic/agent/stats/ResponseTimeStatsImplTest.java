package com.newrelic.agent.stats;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResponseTimeStatsImplTest {

    public static void main(String[] args) {
        System.out.println("Warmup:    " + doEet());
        System.out.println("Benchmark: " + doEet());
    }

    public static long doEet() {
        ResponseTimeStats multithreaded = new ResponseTimeStatsImpl();

        ExecutorService multithreadedExecutor = Executors.newFixedThreadPool(10);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            multithreadedExecutor.submit(() -> {
                for (int j = 0; j < 10_000_000; j++) {
                    multithreaded.recordResponseTimeInNanos(1);
                }
            });
        }
        long end = System.currentTimeMillis();
        multithreadedExecutor.shutdown();
        try {
            if (!multithreadedExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                multithreadedExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            multithreadedExecutor.shutdownNow();
        }

        System.out.println(multithreaded);

        return end - start;
    }
}
