package ru.otus;

import java.util.Map;

public interface WithdrawalStrategy {
    Map<Integer, Integer> withdraw(int amount, Map<Integer, CashCell> availableCells);
}
