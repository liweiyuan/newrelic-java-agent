package com.newrelic.agent.stats;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResponseTimeStatsImplTest {

    public static void main(String[] args) {
        ResponseTimeStatsImpl multithreaded = new ResponseTimeStatsImpl();

        ExecutorService multithreadedExecutor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            multithreadedExecutor.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    multithreaded.recordResponseTimeInNanos(1);
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

        System.out.println(multithreaded);

    }
}
