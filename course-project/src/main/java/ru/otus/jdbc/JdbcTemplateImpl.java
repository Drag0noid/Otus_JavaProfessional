package ru.otus.jdbc;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplateImpl implements JdbcTemplate {
    private final DataSource dataSource;

    public JdbcTemplateImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> Optional<T> executeQuery(String sql, List<Object> params, ResultSetHandler<T> rsHandler) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return Optional.ofNullable(rsHandler.handle(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        }
    }

    @Override
    public <T> List<T> executeSelect(String sql, ResultSetHandler<T> rsHandler) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rsHandler.handle(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Select failed: " + sql, e);
        }
    }

    @Override
    public long executeUpdate(String sql, List<Object> params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            setParams(ps, params);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + sql, e);
        }
    }

    @Override
    public <T> T executeConnection(ConnectionCallback<T> action) throws DataAccessException {
        try (Connection conn = dataSource.getConnection()) {
            return action.doInConnection(conn);
        } catch (SQLException e) {
            throw new DataAccessException("Connection operation failed", e);
        }
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }
}