package ru.otus.orm.migration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MigrationLoader {
    private static final String MIGRATIONS_DIR = "migration";
    private static final Pattern MIGRATION_PATTERN =
            Pattern.compile("^V(\\d+)__([\\w\\s]+)\\.sql$", Pattern.CASE_INSENSITIVE);

    public List<MigrationFile> loadMigrations() throws IOException {
        List<MigrationFile> migrations = new ArrayList<>();

        try {
            List<Path> migrationFiles = listMigrationFiles();

            migrationFiles.sort(Comparator.comparing(this::extractVersion));

            for (Path filePath : migrationFiles) {
                String fileName = filePath.getFileName().toString();
                Matcher matcher = MIGRATION_PATTERN.matcher(fileName);

                if (matcher.matches()) {
                    String version = matcher.group(1);
                    String description = matcher.group(2).replace("_", " ");
                    String sqlContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);

                    migrations.add(new MigrationFile(version, description, sqlContent));
                    System.out.println("Found migration: " + fileName);
                }
            }
        } catch (URISyntaxException e) {
            throw new IOException("Error accessing migrations directory", e);
        }

        return migrations;
    }

    private List<Path> listMigrationFiles() throws URISyntaxException, IOException {
        var migrationsUrl = getClass().getClassLoader().getResource(MIGRATIONS_DIR);
        if (migrationsUrl == null) {
            throw new IOException("Migrations directory not found: " + MIGRATIONS_DIR);
        }

        try (Stream<Path> paths = Files.walk(Paths.get(migrationsUrl.toURI()), 1)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> MIGRATION_PATTERN.matcher(path.getFileName().toString()).matches())
                    .collect(Collectors.toList());
        }
    }

    private int extractVersion(Path path) {
        Matcher matcher = MIGRATION_PATTERN.matcher(path.getFileName().toString());
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}