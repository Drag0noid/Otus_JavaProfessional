package ru.otus.processor;

import org.junit.jupiter.api.Test;
import ru.otus.model.Message;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("squid:S1611")
class EvenSecondExceptionTest {

    @Test
    void test1() {
        Supplier<LocalDateTime> evenTime = () -> LocalDateTime.of(2025, 4, 13, 18, 10, 2);
        Processor processor = new EvenSecondException(evenTime);

        assertThrows(RuntimeException.class, () -> processor.process(new Message.Builder(1L).build()));
        System.out.println("Successful!");
    }

    @Test
    void test2() {
        Supplier<LocalDateTime> oddTime = () -> LocalDateTime.of(2025, 4, 13, 18, 10, 3);
        Processor processor = new EvenSecondException(oddTime);

        assertDoesNotThrow(() -> processor.process(new Message.Builder(1L).build()));
        System.out.println("Successful!");
    }

}