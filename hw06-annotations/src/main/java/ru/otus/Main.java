package ru.otus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Runner runner = new Runner();
        Results Results = runner.run(Example.class);

        log.info("All tests: {}", Results.tests());
        log.info("Working: {}", Results.workingTests());
        log.info("Failed: {}", Results.failedTests());
    }
}