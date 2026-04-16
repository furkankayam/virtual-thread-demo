package com.demo.controller;

import com.demo.model.BenchmarkRequest;
import com.demo.model.BenchmarkResult;
import com.demo.service.BenchmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/benchmark")
@CrossOrigin(origins = "*")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @PostMapping("/compare")
    public ResponseEntity<Map<String, BenchmarkResult>> compare(@RequestBody BenchmarkRequest request)
            throws InterruptedException {

        System.out.printf("▶ Benchmark başlıyor: %d task, %dms I/O delay, pool=%d%n",
            request.taskCount(), request.ioDelayMs(), request.threadPoolSize());

        BenchmarkResult platformResult = benchmarkService.runWithPlatformThreads(
            request.taskCount(), request.ioDelayMs(), request.threadPoolSize()
        );
        System.out.printf("✓ Platform: %dms toplam, %.1f task/sn%n",
            platformResult.totalElapsedMs(), platformResult.throughputPerSecond());

        BenchmarkResult virtualResult = benchmarkService.runWithVirtualThreads(
            request.taskCount(), request.ioDelayMs()
        );
        System.out.printf("✓ Virtual: %dms toplam, %.1f task/sn%n",
            virtualResult.totalElapsedMs(), virtualResult.throughputPerSecond());

        return ResponseEntity.ok(Map.of(
            "platform", platformResult,
            "virtual", virtualResult
        ));
    }

    @PostMapping("/virtual")
    public ResponseEntity<BenchmarkResult> virtualOnly(@RequestBody BenchmarkRequest request)
            throws InterruptedException {
        return ResponseEntity.ok(
            benchmarkService.runWithVirtualThreads(request.taskCount(), request.ioDelayMs())
        );
    }

    @PostMapping("/platform")
    public ResponseEntity<BenchmarkResult> platformOnly(@RequestBody BenchmarkRequest request)
            throws InterruptedException {
        return ResponseEntity.ok(
            benchmarkService.runWithPlatformThreads(request.taskCount(), request.ioDelayMs(), request.threadPoolSize())
        );
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK - JDK: " + System.getProperty("java.version"));
    }
}
