package ru.otus;

import java.util.Map;

public record Results(
        int tests,
        int workingTests,
        int failedTests
) {
}