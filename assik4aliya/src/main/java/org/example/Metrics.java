package org.example;


import java.util.LinkedHashMap;
import java.util.Map;


public class Metrics {
    private final Map<String, Long> counters = new LinkedHashMap<>();
    private long startTime;
    private long endTime;
    public void start() { startTime = System.nanoTime(); }
    public void stop() { endTime = System.nanoTime(); }
    public long elapsed() { return endTime - startTime; }
    public void increment(String key) { counters.put(key, counters.getOrDefault(key, 0L) + 1); }
    public Map<String, Long> snapshot() { return new LinkedHashMap<>(counters); }
}