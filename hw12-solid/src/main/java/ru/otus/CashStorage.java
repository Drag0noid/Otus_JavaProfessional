package ru.otus;

import java.util.*;

public class CashStorage {
    private final Map<Integer, CashCell> cells = new TreeMap<>(Collections.reverseOrder());

    public CashStorage(List<Integer> denominations) {
        for (int d : denominations) {
            cells.put(d, new CashCell(d));
        }
    }

    public void deposit(int denomination, int count) {
        if (!cells.containsKey(denomination)) {
            throw new IllegalArgumentException("Неверный номинал: " + denomination);
        }
        cells.get(denomination).add(count);
    }

    public int getBalance() {
        return cells.values().stream().mapToInt(CashCell::getTotal).sum();
    }

    public Map<Integer, CashCell> getCellsSnapshot() {
        Map<Integer, CashCell> snapshot = new TreeMap<>(Collections.reverseOrder());
        for (var entry : cells.entrySet()) {
            CashCell original = entry.getValue();
            CashCell copy = new CashCell(original.getDenomination());
            copy.add(original.getCount());
            snapshot.put(entry.getKey(), copy);
        }
        return snapshot;
    }

    public void applyWithdrawal(Map<Integer, Integer> withdrawal) {
        for (var entry : withdrawal.entrySet()) {
            cells.get(entry.getKey()).remove(entry.getValue());
        }
    }

    public void printState() {
        System.out.println("Состояние банкомата:");
        for (CashCell cell : cells.values()) {
            System.out.println(cell.getDenomination() + " -> " + cell.getCount());
        }
    }
}

