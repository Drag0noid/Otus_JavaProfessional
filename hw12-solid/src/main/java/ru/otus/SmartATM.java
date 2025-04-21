package ru.otus;

import java.util.*;

public class SmartATM implements ATM {
    private final CashStorage storage;
    private final WithdrawalStrategy strategy;

    public SmartATM(List<Integer> denominations) {
        this.storage = new CashStorage(denominations);
        this.strategy = new MinimalNotesWithdrawalStrategy(); // можно заменить на другую стратегию
    }

    @Override
    public void deposit(int denomination, int count) {
        storage.deposit(denomination, count);
    }

    @Override
    public void withdraw(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Сумма должна быть положительной");
        Map<Integer, CashCell> snapshot = storage.getCellsSnapshot();
        Map<Integer, Integer> withdrawal = strategy.withdraw(amount, snapshot);
        storage.applyWithdrawal(withdrawal);

        System.out.println("Выдано:");
        withdrawal.forEach((k, v) -> System.out.println(k + " x " + v));
    }

    @Override
    public int getBalance() {
        return storage.getBalance();
    }

    public void printState() {
        storage.printState();
    }
}
