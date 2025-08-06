package ru.otus;

import ru.otus.crm.datasource.DriverManagerDataSource;
import ru.otus.orm.migration.MigrationRunner;

import javax.sql.DataSource;

public class Application {
    public static void main(String[] args) {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:postgresql://localhost:5432/hwdb",
                "postgres",
                "12345"
        );

        try {
            new MigrationRunner(dataSource).runMigrations();
        } catch (Exception e) {
            System.err.println("Migrations failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }

    }
}