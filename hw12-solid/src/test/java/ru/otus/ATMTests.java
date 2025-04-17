package ru.otus;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ATMTests {

    @Test
    @DisplayName("1. Пополнение банкомата работает корректно")
    void depositTest() {
        SmartATM atm = new SmartATM(Arrays.asList(50, 100));
        atm.deposit(50, 2);
        atm.deposit(100, 3);
        assertEquals(400, atm.getBalance());
    }

    @Test
    @DisplayName("2. Баланс корректно считается")
    void balanceTest() {
        SmartATM atm = new SmartATM(Arrays.asList(1000, 500));
        atm.deposit(1000, 1); // 1000
        atm.deposit(500, 2);  // 1000
        assertEquals(2000, atm.getBalance());
    }

    @Test
    @DisplayName("3. Выдача наличных работает")
    void withdrawSuccessTest() {
        SmartATM atm = new SmartATM(Arrays.asList(100, 500));
        atm.deposit(100, 5);  // 500
        atm.deposit(500, 1);  // 500
        atm.withdraw(600);    // должен выдать 500 + 100
        assertEquals(400, atm.getBalance());
    }

    @Test
    @DisplayName("4. Ошибка при невозможности выдать сумму")
    void withdrawImpossibleAmountTest() {
        SmartATM atm = new SmartATM(Arrays.asList(100, 500));
        atm.deposit(100, 1); // 100
        Exception ex = assertThrows(IllegalStateException.class, () -> {
            atm.withdraw(150); // нельзя выдать 150
        });
        assertEquals("Невозможно выдать запрошенную сумму", ex.getMessage());
    }

    @Test
    @DisplayName("5. Ошибка при пополнении недопустимым номиналом")
    void invalidDenominationTest() {
        SmartATM atm = new SmartATM(Arrays.asList(100, 500));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            atm.deposit(200, 1); // 200 не зарегистрирован
        });
        assertEquals("Неверный номинал: 200", ex.getMessage());
    }

    @Test
    @DisplayName("6. Ошибка при попытке снять 0 или меньше")
    void invalidWithdrawRequestTest() {
        SmartATM atm = new SmartATM(Arrays.asList(100, 500));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            atm.withdraw(0);
        });
        assertEquals("Сумма должна быть положительной", ex.getMessage());
    }

    @Test
    @DisplayName("7. Пополнение нулевым количеством банкнот должно падать")
    void zeroDepositTest() {
        SmartATM atm = new SmartATM(Arrays.asList(100));
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            atm.deposit(100, 0);
        });
        assertEquals("Неверное количество банкнот", ex.getMessage());
    }
}
