package ru.otus;

import java.util.*;

public class MinimalNotesWithdrawalStrategy implements WithdrawalStrategy {

    @Override
    public Map<Integer, Integer> withdraw(int amount, Map<Integer, CashCell> availableCells) {
        Map<Integer, Integer> result = new TreeMap<>(Collections.reverseOrder());

        for (var entry : availableCells.entrySet()) {
            int denom = entry.getKey();
            int availableCount = entry.getValue().getCount();

            int needed = amount / denom;
            if (needed > 0) {
                int take = Math.min(needed, availableCount);
                if (take > 0) {
                    result.put(denom, take);
                    amount -= denom * take;
                }
            }
        }

        if (amount != 0) {
            throw new IllegalStateException("Невозможно выдать запрошенную сумму");
        }

        return result;
    }
}
