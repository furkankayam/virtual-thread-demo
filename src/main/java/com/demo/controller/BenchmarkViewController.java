package com.demo.controller;

import com.demo.model.BenchmarkRequest;
import com.demo.model.BenchmarkResult;
import com.demo.service.BenchmarkService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BenchmarkViewController {

    private final BenchmarkService benchmarkService;

    public BenchmarkViewController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @GetMapping("/")
    public String index() {
        return "benchmark";
    }

    @PostMapping("/benchmark")
    public String runBenchmark(
            @RequestParam(defaultValue = "300") int taskCount,
            @RequestParam(defaultValue = "100") long ioDelayMs,
            @RequestParam(defaultValue = "50") int threadPoolSize,
            Model model) throws InterruptedException {

        BenchmarkRequest request = new BenchmarkRequest(taskCount, ioDelayMs, threadPoolSize);

        BenchmarkResult platform = benchmarkService.runWithPlatformThreads(
            request.taskCount(), request.ioDelayMs(), request.threadPoolSize()
        );
        BenchmarkResult virtual = benchmarkService.runWithVirtualThreads(
            request.taskCount(), request.ioDelayMs()
        );

        double speedup = (double) platform.totalElapsedMs() / virtual.totalElapsedMs();
        int rounds = (int) Math.ceil((double) taskCount / threadPoolSize);

        model.addAttribute("platform", platform);
        model.addAttribute("virtual", virtual);
        model.addAttribute("speedup", String.format("%.1f", speedup));
        model.addAttribute("poolSize", threadPoolSize);
        model.addAttribute("roundRobinRounds", rounds);

        return "benchmark";
    }
}
