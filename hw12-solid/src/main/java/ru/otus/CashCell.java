package ru.otus;

public class CashCell {
    private final int denomination;
    private int count;

    public CashCell(int denomination) {
        this.denomination = denomination;
    }

    public int getDenomination() {
        return denomination;
    }

    public int getCount() {
        return count;
    }

    public void add(int count) {
        if (count <= 0) throw new IllegalArgumentException("Неверное количество банкнот");
        this.count += count;
    }

    public int remove(int countToRemove) {
        int removed = Math.min(countToRemove, this.count);
        this.count -= removed;
        return removed;
    }

    public int getTotal() {
        return denomination * count;
    }
}

