package ru.otus.orm.migration.exceptions;

import java.sql.SQLException;

public class MigrationException extends RuntimeException {
    public MigrationException(String message, SQLException e) {
        super(message, e);
    }

    public MigrationException(String message, Exception e) {
        super(message, e);
    }

    public MigrationException(String message) {
        super(message);
    }
}