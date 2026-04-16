package com.demo.model;

public record BenchmarkRequest(
    int taskCount,
    long ioDelayMs,
    int threadPoolSize
) {
    public BenchmarkRequest {
        if (taskCount <= 0) taskCount = 200;
        if (ioDelayMs <= 0) ioDelayMs = 100;
        if (threadPoolSize <= 0) threadPoolSize = 50;
        if (taskCount > 2000) taskCount = 2000;
    }
}
