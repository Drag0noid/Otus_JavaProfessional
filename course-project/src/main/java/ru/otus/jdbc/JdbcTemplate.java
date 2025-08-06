package ru.otus.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface JdbcTemplate {
    @FunctionalInterface
    interface ResultSetHandler<T> {
        T handle(ResultSet rs) throws SQLException;
    }

    <T> Optional<T> executeQuery(String sql, List<Object> params, ResultSetHandler<T> rsHandler);
    <T> List<T> executeSelect(String sql, ResultSetHandler<T> rsHandler);
    long executeUpdate(String sql, List<Object> params);
    <T> T executeConnection(ConnectionCallback<T> action) throws DataAccessException;

    @FunctionalInterface
    interface ConnectionCallback<T> {
        T doInConnection(Connection connection) throws SQLException;
    }
}