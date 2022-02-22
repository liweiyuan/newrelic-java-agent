/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.newrelic.agent.stats;

import com.newrelic.agent.Agent;
import com.newrelic.agent.config.AgentConfigImpl;
import com.newrelic.agent.util.TimeConversion;
import com.newrelic.api.agent.NewRelic;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * This class is not thread-safe.
 */
public class ResponseTimeStatsAtomicImpl implements ResponseTimeStats {

    private static final long NANOSECONDS_PER_SECOND_SQUARED = TimeConversion.NANOSECONDS_PER_SECOND
            * TimeConversion.NANOSECONDS_PER_SECOND;

    // FIXME:  Atomic or synchronize?
    private AtomicLong total = new AtomicLong(0);
    private AtomicLong totalExclusive = new AtomicLong(0);
    private AtomicLong minValue = new AtomicLong(0);
    private AtomicLong maxValue = new AtomicLong(0);
    private AtomicLong sumOfSquares = new AtomicLong(0);

    protected AtomicInteger count = new AtomicInteger(0);

    @Override
    public Object clone() throws CloneNotSupportedException {
        ResponseTimeStatsAtomicImpl newStats = new ResponseTimeStatsAtomicImpl();
        newStats.count = new AtomicInteger(count.get());
        newStats.total = new AtomicLong(total.get());
        newStats.totalExclusive = new AtomicLong(totalExclusive.get());
        newStats.minValue = new AtomicLong(minValue.get());
        newStats.maxValue = new AtomicLong(maxValue.get());
        newStats.sumOfSquares = new AtomicLong(sumOfSquares.get());
        return newStats;
    }

    @Override
    public void recordResponseTime(long responseTime, TimeUnit timeUnit) {
        long responseTimeInNanos = TimeUnit.NANOSECONDS.convert(responseTime, timeUnit);
        recordResponseTimeInNanos(responseTimeInNanos, responseTimeInNanos);
    }

    @Override
    public void recordResponseTime(long responseTime, long exclusiveTime, TimeUnit timeUnit) {
        long responseTimeInNanos = TimeUnit.NANOSECONDS.convert(responseTime, timeUnit);
        long exclusiveTimeInNanos = TimeUnit.NANOSECONDS.convert(exclusiveTime, timeUnit);
        recordResponseTimeInNanos(responseTimeInNanos, exclusiveTimeInNanos);
    }

    @Override
    public void recordResponseTimeInNanos(long responseTime) {
        recordResponseTimeInNanos(responseTime, responseTime);
    }

    @Override
    public void recordResponseTimeInNanos(long responseTime, long exclusiveTime) {
        // Why was this a double?
        long responseTimeAsDouble = responseTime;
        responseTimeAsDouble *= responseTimeAsDouble;
        sumOfSquares.addAndGet(responseTimeAsDouble);
        if (count.get() > 0) {
            minValue.set(Math.min(responseTime, minValue.get()));
        } else {
            minValue.set(responseTime);
        }
        count.incrementAndGet();
        total.addAndGet(responseTime);
        maxValue.set(Math.max(responseTime, maxValue.get()));
        totalExclusive.addAndGet(exclusiveTime);
        if (NewRelic.getAgent().getConfig().getValue(AgentConfigImpl.METRIC_DEBUG, AgentConfigImpl.DEFAULT_METRIC_DEBUG)) {
            if (count.get() < 0 || total.get() < 0 || totalExclusive.get() < 0 || sumOfSquares.get() < 0) {
                NewRelic.incrementCounter("Supportability/ResponseTimeStatsImpl/NegativeValue");
                Agent.LOG.log(Level.INFO, "Invalid count {0}, total {1}, totalExclusive {2}, or sum of squares {3}",
                        count, total, totalExclusive, sumOfSquares);
            }
        }

    }

    @Override
    public boolean hasData() {
        return count.get() > 0 || total.get() > 0 || totalExclusive.get() > 0;
    }

    @Override
    public void reset() {
        count.set(0);
        total.set(0);
        totalExclusive.set(0);
        minValue.set(0);
        maxValue.set(0);
        sumOfSquares.set(0);
    }

    @Override
    public void incrementCallCount(int value) {
        count.addAndGet(value);
    }

    @Override
    public void incrementCallCount() {
        this.count.incrementAndGet();
    }

    @Override
    public int getCallCount() {
        return count.get();
    }

    @Override
    public void setCallCount(int count) {
        this.count.set(count);
    }

    // why are these floats and not doubles?
    @Override
    public float getTotal() {
        return (float) total.get() / TimeConversion.NANOSECONDS_PER_SECOND;
    }

    @Override
    public float getTotalExclusiveTime() {
        return (float) totalExclusive.get() / TimeConversion.NANOSECONDS_PER_SECOND;
    }

    @Override
    public float getMaxCallTime() {
        return (float) maxValue.get() / TimeConversion.NANOSECONDS_PER_SECOND;
    }

    @Override
    public float getMinCallTime() {
        return (float) minValue.get() / TimeConversion.NANOSECONDS_PER_SECOND;
    }

    @Override
    public double getSumOfSquares() {
        return sumOfSquares.get() / NANOSECONDS_PER_SECOND_SQUARED;
    }

    @Override
    public final void merge(StatsBase statsObj) {
        if (statsObj instanceof ResponseTimeStatsAtomicImpl) {
            ResponseTimeStatsAtomicImpl stats = (ResponseTimeStatsAtomicImpl) statsObj;
            if (stats.count.get() > 0) {
                if (count.get() > 0) {
                    minValue.set(Math.min(minValue.get(), stats.minValue.get()));
                } else {
                    minValue.set(stats.minValue.get());
                }
            }
            count.addAndGet(stats.count.get());
            total.addAndGet(stats.total.get());
            totalExclusive.addAndGet(stats.totalExclusive.get());

            maxValue.set(Math.max(maxValue.get(), stats.maxValue.get()));
            sumOfSquares.addAndGet(stats.sumOfSquares.get());
        }
    }

    @Override
    public void recordResponseTime(int count, long totalTime, long minTime, long maxTime, TimeUnit unit) {
        long totalTimeInNanos = TimeUnit.NANOSECONDS.convert(totalTime, unit);
        this.count.set(count);
        this.total.set(totalTimeInNanos);
        this.totalExclusive.set(totalTimeInNanos);
        this.minValue.set(TimeUnit.NANOSECONDS.convert(minTime, unit));
        this.maxValue.set(TimeUnit.NANOSECONDS.convert(maxTime, unit));
        // Why double?
        long totalTimeInNanosAsDouble = totalTimeInNanos;
        totalTimeInNanosAsDouble *= totalTimeInNanosAsDouble;
        sumOfSquares.addAndGet(totalTimeInNanosAsDouble);
    }

    @Override
    public String toString() {
        return "ResponseTimeStatsImpl [total=" + total.get() + ", totalExclusive=" + totalExclusive + ", minValue="
                + minValue + ", maxValue=" + maxValue + ", sumOfSquares=" + sumOfSquares + "]";

    }

    @Override
    public void writeJSONString(Writer out) throws IOException {

    }
}
