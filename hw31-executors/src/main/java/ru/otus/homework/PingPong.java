package ru.otus.homework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingPong {
    private static final Logger logger = LoggerFactory.getLogger(PingPong.class);

    private final Object monitor = new Object();
    private boolean isFirstThreadTurn = true;

    private int currentNumber = 1;
    private int step = 1;

    public static void main(String[] args) {
        PingPong pingPong = new PingPong();

        Thread thread1 = new Thread(pingPong::printFromFirstThread, "Поток-1");
        Thread thread2 = new Thread(pingPong::printFromSecondThread, "Поток-2");

        thread1.start();
        thread2.start();
    }

    private void printFromFirstThread() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (monitor) {
                while (!isFirstThreadTurn) {
                    waitForTurn();
                }

                logger.info("{}", currentNumber);

                isFirstThreadTurn = false;
                monitor.notifyAll();
                sleep();
            }
        }
    }

    private void printFromSecondThread() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (monitor) {
                while (isFirstThreadTurn) {
                    waitForTurn();
                }

                logger.info("{}", currentNumber);

                updateNumber();

                isFirstThreadTurn = true;
                monitor.notifyAll();
                sleep();
            }
        }
    }

    private void updateNumber() {
        if (currentNumber == 10) {
            step = -1;
        } else if (currentNumber == 1) {
            step = 1;
        }
        currentNumber += step;
    }

    private void waitForTurn() {
        try {
            monitor.wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
