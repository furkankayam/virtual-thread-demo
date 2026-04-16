package com.demo.model;

import java.util.List;

public record BenchmarkResult(
    String label,
    int taskCount,
    long ioDelayMs,
    long totalElapsedMs,
    double avgTaskMs,
    double throughputPerSecond,
    int successCount,
    int failCount,
    List<TaskResult> taskResults
) {}
