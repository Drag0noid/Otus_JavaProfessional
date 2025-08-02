package ru.otus.orm.migration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class MigrationFile {
    private final String version;
    private final String description;
    private final String sqlContent;
    private final String checksum;

    public MigrationFile(String version, String description, String sqlContent) {
        this.version = Objects.requireNonNull(version, "Version cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.sqlContent = Objects.requireNonNull(sqlContent, "SQL content cannot be null");
        this.checksum = calculateChecksum(sqlContent);
    }

    private String calculateChecksum(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(content.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getSqlContent() {
        return sqlContent;
    }

    public String getChecksum() {
        return checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrationFile that = (MigrationFile) o;
        return version.equals(that.version) &&
                checksum.equals(that.checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, checksum);
    }

    @Override
    public String toString() {
        return "MigrationFile{" +
                "version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}