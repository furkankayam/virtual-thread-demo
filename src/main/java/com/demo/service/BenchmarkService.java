package com.demo.service;

import com.demo.model.BenchmarkResult;
import com.demo.model.TaskResult;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BenchmarkService {

    private TaskResult simulateIoTask(int taskId, long ioDelayMs) {
        Instant start = Instant.now();
        try {
            Thread.sleep(ioDelayMs);
            return new TaskResult(
                taskId,
                Thread.currentThread().isVirtual() ? "virtual" : "platform",
                Thread.currentThread().getName(),
                Duration.between(start, Instant.now()).toMillis(),
                true,
                null
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TaskResult(taskId, "unknown", Thread.currentThread().getName(),
                Duration.between(start, Instant.now()).toMillis(), false, e.getMessage());
        }
    }

    public BenchmarkResult runWithPlatformThreads(int taskCount, long ioDelayMs, int threadPoolSize) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        return runBenchmark(executor, taskCount, ioDelayMs, "Platform Threads (pool=" + threadPoolSize + ")");
    }

    public BenchmarkResult runWithVirtualThreads(int taskCount, long ioDelayMs) throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        return runBenchmark(executor, taskCount, ioDelayMs, "Virtual Threads");
    }

    private BenchmarkResult runBenchmark(ExecutorService executor, int taskCount, long ioDelayMs, String label)
            throws InterruptedException {

        List<Future<TaskResult>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Instant globalStart = Instant.now();

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            futures.add(executor.submit(() -> simulateIoTask(taskId, ioDelayMs)));
        }

        List<TaskResult> results = new ArrayList<>();
        for (Future<TaskResult> future : futures) {
            try {
                TaskResult result = future.get(60, TimeUnit.SECONDS);
                results.add(result);
                if (result.success()) successCount.incrementAndGet();
                else failCount.incrementAndGet();
            } catch (ExecutionException | TimeoutException e) {
                failCount.incrementAndGet();
            }
        }

        long totalElapsedMs = Duration.between(globalStart, Instant.now()).toMillis();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        double avgTaskMs = results.stream()
            .mapToLong(TaskResult::elapsedMs)
            .average()
            .orElse(0);

        double throughput = taskCount / (totalElapsedMs / 1000.0);

        return new BenchmarkResult(
            label,
            taskCount,
            ioDelayMs,
            totalElapsedMs,
            avgTaskMs,
            throughput,
            successCount.get(),
            failCount.get(),
            results
        );
    }
}
