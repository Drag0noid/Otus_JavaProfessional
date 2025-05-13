package ru.otus.core.repository;

public class DataTemplateException extends RuntimeException {
    public DataTemplateException(String message) {
        super(message);
    }

    public DataTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataTemplateException(Throwable cause) {
        super(cause);
    }
}
