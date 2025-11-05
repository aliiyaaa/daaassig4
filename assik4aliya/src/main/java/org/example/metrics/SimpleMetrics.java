package org.example.metrics;


import java.util.LinkedHashMap;
import java.util.Map;


public class SimpleMetrics implements Metrics {
    private final Map<String, Long> counters = new LinkedHashMap<>();
    private long startTime;
    private long endTime;

    @Override
    public void start() { startTime = System.nanoTime(); }

    @Override
    public void stop() { endTime = System.nanoTime(); }

    @Override
    public long elapsed() { return endTime - startTime; }

    @Override
    public void increment(String key) { counters.put(key, counters.getOrDefault(key, 0L) + 1); }

    @Override
    public Map<String, Long> snapshot() { return new LinkedHashMap<>(counters); }
}


