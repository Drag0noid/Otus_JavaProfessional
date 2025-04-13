package ru.otus.processor;

import ru.otus.model.Message;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class EvenSecondException implements Processor {

    private final Supplier<LocalDateTime> timeProvider;

    public EvenSecondException(Supplier<LocalDateTime> timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public Message process(Message message) {
        int msg = timeProvider.get().getSecond();
        if (msg % 2 == 0) {
            throw new RuntimeException(String.valueOf(msg));
        }
        return message;
    }
}