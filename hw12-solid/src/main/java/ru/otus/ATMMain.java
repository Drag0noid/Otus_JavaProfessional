package ru.otus;

import java.util.*;

public class ATMMain {
    public static void main(String[] args) {
        SmartATM atm = new SmartATM(Arrays.asList(50, 100, 500, 1000));

        atm.printState();

        System.out.println("Баланс: " + atm.getBalance());

        atm.deposit(100, 10);
        atm.deposit(500, 5);
        atm.deposit(50, 20);

        atm.printState();

        System.out.println("Баланс: " + atm.getBalance());

        try {
            atm.withdraw(1150);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        atm.printState();

        System.out.println("Баланс: " + atm.getBalance());
    }
}
