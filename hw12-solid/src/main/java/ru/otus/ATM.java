package ru.otus;

public interface ATM {
    void deposit(int denomination, int count);
    void withdraw(int amount);
    int getBalance();
}

