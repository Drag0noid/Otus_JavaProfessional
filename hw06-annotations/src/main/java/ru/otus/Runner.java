package ru.otus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Runner {

    private final Logger log = LoggerFactory.getLogger(Runner.class);

    public Results run(Class<?> testClass) throws Exception {
        List<Method> beforeMethods = getMethodsAnnotatedBy(testClass, Before.class);
        List<Method> testMethods = getMethodsAnnotatedBy(testClass, Test.class);
        List<Method> afterMethods = getMethodsAnnotatedBy(testClass, After.class);

        int allTests = testMethods.size();
        int passedTests = 0;
        Map<String, Boolean> results = new LinkedHashMap<>();
        Map<String, String> errorMessages = new LinkedHashMap<>();

        for (Method testMethod : testMethods) {
            Object testObject = testClass.getDeclaredConstructor().newInstance();
            String testName = testMethod.getName();
            try {
                invokeMethods(beforeMethods, testObject);
                testMethod.invoke(testObject);
                invokeMethods(afterMethods, testObject);
                passedTests++;
                results.put(testName, true);
                errorMessages.put(testName, "");
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getTargetException();
                results.put(testName, false);
                errorMessages.put(testName, cause.toString());
                invokeMethods(afterMethods, testObject);
            }
        }

        printReport(results, errorMessages);

        return new Results(allTests, passedTests, allTests - passedTests);
    }

    private void invokeMethods(List<Method> methods, Object testObject) throws Exception {
        for (Method method : methods) {
            method.invoke(testObject);
        }
    }

    private List<Method> getMethodsAnnotatedBy(Class<?> testClass, Class<? extends Annotation> annotation) {
        return Arrays.stream(testClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotation))
                .toList();
    }

    private void printReport(Map<String, Boolean> results, Map<String, String> errors) {
        System.out.println("\n================== TEST REPORT ==================");
        results.forEach((testName, passed) -> {
            String result = passed ? "PASSED" : "FAILED";
            String message = errors.get(testName);
            System.out.printf("%-15s | %-8s | %-40s%n", testName, result, message);
        });
        System.out.println("---------------------------------------------------------------");
        long passedCount = results.values().stream().filter(r -> r).count();
        long failedCount = results.size() - passedCount;
        System.out.printf("TOTAL: %d | PASSED: %d | FAILED: %d%n", results.size(), passedCount, failedCount);
    }
}
