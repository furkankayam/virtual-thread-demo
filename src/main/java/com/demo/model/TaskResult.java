package com.demo.model;

public record TaskResult(
    int taskId,
    String threadType,
    String threadName,
    long elapsedMs,
    boolean success,
    String errorMessage
) {}
