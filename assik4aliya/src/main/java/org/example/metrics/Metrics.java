package org.example.metrics;


import java.util.Map;


public interface Metrics {
    void start();
    void stop();
    long elapsed();
    void increment(String key);
    Map<String, Long> snapshot();
}


