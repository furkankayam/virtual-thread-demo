package com.demo.service;

import com.demo.model.BenchmarkResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BenchmarkServiceTest {

    private final BenchmarkService benchmarkService = new BenchmarkService();

    @Test
    void virtualThreads_shouldCompleteAllTasks() throws InterruptedException {
        int taskCount = 50;
        long ioDelayMs = 50;

        BenchmarkResult result = benchmarkService.runWithVirtualThreads(taskCount, ioDelayMs);

        assertThat(result.successCount()).isEqualTo(taskCount);
        assertThat(result.failCount()).isZero();
        assertThat(result.totalElapsedMs()).isLessThan(taskCount * ioDelayMs);
    }

    @Test
    void platformThreads_withSmallPool_shouldTakeMoreTime() throws InterruptedException {
        int taskCount = 50;
        long ioDelayMs = 50;
        int poolSize = 5;

        BenchmarkResult result = benchmarkService.runWithPlatformThreads(taskCount, ioDelayMs, poolSize);

        assertThat(result.successCount()).isEqualTo(taskCount);
        assertThat(result.totalElapsedMs()).isGreaterThanOrEqualTo((taskCount / poolSize) * ioDelayMs);
    }

    @Test
    void virtualThreads_shouldBeFasterThanSmallPlatformPool() throws InterruptedException {
        int taskCount = 100;
        long ioDelayMs = 50;

        BenchmarkResult virtual = benchmarkService.runWithVirtualThreads(taskCount, ioDelayMs);
        BenchmarkResult platform = benchmarkService.runWithPlatformThreads(taskCount, ioDelayMs, 10);

        System.out.printf("Virtual: %dms | Platform(pool=10): %dms%n",
            virtual.totalElapsedMs(), platform.totalElapsedMs());

        assertThat(virtual.totalElapsedMs()).isLessThan(platform.totalElapsedMs());
    }

    @Test
    void virtualThreads_tasksShouldRunOnVirtualThreads() throws InterruptedException {
        BenchmarkResult result = benchmarkService.runWithVirtualThreads(10, 10);

        result.taskResults().forEach(task ->
            assertThat(task.threadType()).isEqualTo("virtual")
        );
    }

    @Test
    void platformThreads_tasksShouldRunOnPlatformThreads() throws InterruptedException {
        BenchmarkResult result = benchmarkService.runWithPlatformThreads(10, 10, 5);

        result.taskResults().forEach(task ->
            assertThat(task.threadType()).isEqualTo("platform")
        );
    }
}
