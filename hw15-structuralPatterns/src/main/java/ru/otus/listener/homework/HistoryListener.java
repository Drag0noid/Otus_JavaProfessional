package ru.otus.listener.homework;

import ru.otus.listener.Listener;
import ru.otus.model.Message;
import ru.otus.model.ObjectForMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class HistoryListener implements Listener, HistoryReader {

    private final Map<Long, Message> history = new ConcurrentHashMap<>();

    @Override
    public void onUpdated(Message msg) {
        history.put(msg.getId(), msg.toBuilder()
                .field13(msg.getField13() == null ? null : cloneField13(msg))
                .build());
    }

    private ObjectForMessage cloneField13(Message msg) {
        ObjectForMessage original = msg.getField13();
        if (original == null) return null;
        ObjectForMessage clone = new ObjectForMessage();
        if (original.getData() != null) {
            clone.setData(List.copyOf(original.getData()));
        }
        return clone;
    }

    @Override
    public Optional<Message> findMessageById(long id) {
        return Optional.ofNullable(history.get(id));
    }
}