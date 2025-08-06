package ru.otus.orm.migration;

import ru.otus.orm.migration.exceptions.MigrationException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

public class MigrationRunner {
    private final DataSource dataSource;

    public MigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void runMigrations() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                createMigrationsTable(conn);
                List<MigrationFile> migrations = new MigrationLoader().loadMigrations();
                applyMigrationsWithStateVerification(conn, migrations);
                conn.commit();
                System.out.println("\nDatabase migrations completed successfully!");
            } catch (Exception e) {
                conn.rollback();
                System.err.println("\nMigration error details:");
                e.printStackTrace();
                throw new MigrationException("Migration failed", e);
            }
        } catch (SQLException e) {
            throw new MigrationException("Database connection error", e);
        }
    }

    private boolean shouldExecuteDDL(Connection conn, String query) throws SQLException {
        String normalized = query.trim().toUpperCase();

        if (normalized.startsWith("CREATE TABLE")) {
            String tableName = extractTableNameFromDDL(query);
            return !tableExists(conn, tableName);
        }
        else if (normalized.startsWith("ALTER TABLE") && normalized.contains("ADD COLUMN")) {
            String[] parts = query.split(" ");
            String tableName = parts[2];
            String columnName = parts[parts.length-1];
            return !columnExists(conn, tableName, columnName);
        }

        return true;
    }

    private String extractTableNameFromDDL(String query) {
        String[] parts = query.split(" ");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("TABLE") && i < parts.length - 1) {
                return parts[i+1].replace(";", "").trim();
            }
        }
        return "";
    }

    private void createMigrationsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS schema_migrations (" +
                "version VARCHAR(50) PRIMARY KEY," +
                "description TEXT NOT NULL," +
                "checksum VARCHAR(64) NOT NULL," +
                "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL" +
                ")";
        executeUpdate(conn, sql);
    }

    private void applyMigrationsWithStateVerification(Connection conn,
                                                      List<MigrationFile> migrations) throws SQLException {
        for (MigrationFile migration : migrations) {
            MigrationStatus status = checkMigrationStatus(conn, migration);

            switch (status) {
                case NOT_APPLIED:
                    boolean hasChanges = executeMigrationAndCheckChanges(conn, migration);
                    if (hasChanges) {
                        recordMigration(conn, migration);
                    }
                    break;

                case APPLIED_CORRECTLY:
                    break;

                case APPLIED_PARTIALLY:
                    boolean hadChanges = executeMigrationAndCheckChanges(conn, migration);
                    if (hadChanges) {
                        updateMigrationRecord(conn, migration);
                    }
                    break;
            }
        }
    }

    private boolean executeMigrationAndCheckChanges(Connection conn, MigrationFile migration) throws SQLException {
        String[] queries = migration.getSqlContent().split(";\\s*");
        boolean hasChanges = false;

        for (String query : queries) {
            if (!query.trim().isEmpty()) {
                try (Statement stmt = conn.createStatement()) {
                    if (isDataModificationQuery(query)) {
                        int rowsAffected = stmt.executeUpdate(query.trim());
                        if (rowsAffected > 0) {
                            hasChanges = true;
                        }
                    } else {
                        if (shouldExecuteDDL(conn, query)) {
                            stmt.execute(query.trim());
                            hasChanges = true;
                        }
                    }
                }
            }
        }
        return hasChanges;
    }

    private MigrationStatus checkMigrationStatus(Connection conn,
                                                 MigrationFile migration) throws SQLException {
        boolean isRecorded = isMigrationRecorded(conn, migration.getVersion());
        boolean isChecksumValid = isChecksumValid(conn, migration);
        boolean isDatabaseStateCorrect = checkDatabaseState(conn, migration);

        if (!isRecorded) {
            return MigrationStatus.NOT_APPLIED;
        } else if (!isDatabaseStateCorrect || !isChecksumValid) {
            return MigrationStatus.APPLIED_PARTIALLY;
        } else {
            return MigrationStatus.APPLIED_CORRECTLY;
        }
    }

    private boolean isMigrationRecorded(Connection conn, String version) throws SQLException {
        String sql = "SELECT 1 FROM schema_migrations WHERE version = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isChecksumValid(Connection conn, MigrationFile migration) throws SQLException {
        String sql = "SELECT checksum FROM schema_migrations WHERE version = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, migration.getVersion());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1).equals(migration.getChecksum());
                }
                return false;
            }
        }
    }

    private boolean checkDatabaseState(Connection conn, MigrationFile migration) throws SQLException {
        if (migration.getDescription().toLowerCase().contains("create") &&
                migration.getDescription().toLowerCase().contains("table")) {
            String tableName = extractTableName(migration.getDescription());
            return tableExists(conn, tableName);
        }
        else if (migration.getDescription().toLowerCase().contains("add") &&
                migration.getDescription().toLowerCase().contains("column")) {
            String[] parts = migration.getDescription().split(" ");
            String tableName = parts[parts.length-3];
            String columnName = parts[parts.length-1];
            return columnExists(conn, tableName, columnName);
        }
        else if (migration.getDescription().toLowerCase().contains("insert")) {
            // Для INSERT-миграций проверяем, есть ли данные
            return checkInsertMigration(conn, migration);
        }

        return true;
    }

    private boolean checkInsertMigration(Connection conn, MigrationFile migration) throws SQLException {
        String tableName = extractTableName(migration.getDescription());
        if (!tableName.isEmpty()) {
            String countSql = "SELECT COUNT(*) FROM " + tableName;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSql)) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private String extractTableName(String description) {
        String[] parts = description.toLowerCase().split(" ");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("table") && i < parts.length - 1) {
                return parts[i+1];
            }
            if (parts[i].equals("into") && i < parts.length - 1) {
                return parts[i+1];
            }
        }
        return "";
    }

    private void executeMigration(Connection conn, MigrationFile migration) throws SQLException {
        String[] queries = migration.getSqlContent().split(";\\s*");

        for (String query : queries) {
            if (!query.trim().isEmpty()) {
                try (Statement stmt = conn.createStatement()) {
                    if (isDataModificationQuery(query)) {
                        int rowsAffected = stmt.executeUpdate(query.trim());
                        if (rowsAffected > 0) {
                            updateMigrationTimestamp(conn, migration);
                        } else {
                            System.out.println("No rows affected - skipping timestamp update");
                        }
                    } else {
                        stmt.execute(query.trim());
                        updateMigrationTimestamp(conn, migration);
                    }
                }
            }
        }
    }

    private void updateMigrationTimestamp(Connection conn, MigrationFile migration) throws SQLException {
        String checkSql = "SELECT 1 FROM schema_migrations WHERE version = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, migration.getVersion());
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    String updateSql = "UPDATE schema_migrations SET applied_at = CURRENT_TIMESTAMP WHERE version = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, migration.getVersion());
                        updatePs.executeUpdate();
                    }
                } else {
                    recordMigration(conn, migration);
                }
            }
        }
    }

    public boolean isDataModificationQuery(String query) {
        String normalized = query.trim().toUpperCase();
        return normalized.startsWith("INSERT") ||
                normalized.startsWith("UPDATE") ||
                normalized.startsWith("DELETE");
    }

    private void recordMigration(Connection conn, MigrationFile migration) throws SQLException {
        String sql = "INSERT INTO schema_migrations(version, description, checksum) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, migration.getVersion());
            ps.setString(2, migration.getDescription());
            ps.setString(3, migration.getChecksum());
            ps.executeUpdate();
        }
    }

    private void updateMigrationRecord(Connection conn, MigrationFile migration) throws SQLException {
        String sql = "UPDATE schema_migrations SET " +
                "description = ?, " +
                "checksum = ?, " +
                "applied_at = CURRENT_TIMESTAMP " +
                "WHERE version = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, migration.getDescription());
            ps.setString(2, migration.getChecksum());
            ps.setString(3, migration.getVersion());
            ps.executeUpdate();
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(
                null, null, tableName.toLowerCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(
                null, null, tableName.toLowerCase(), columnName.toLowerCase())) {
            return rs.next();
        }
    }

    private void executeUpdate(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private enum MigrationStatus {
        NOT_APPLIED,
        APPLIED_CORRECTLY,
        APPLIED_PARTIALLY
    }
}