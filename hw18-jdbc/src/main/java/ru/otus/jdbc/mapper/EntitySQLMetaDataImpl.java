package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {
    private final EntityClassMetaData<?> metaData;
    private final String tableName;
    private final String idFieldName;
    private final List<String> columnNames;

    public EntitySQLMetaDataImpl(EntityClassMetaData<?> metaData) {
        this.metaData = metaData;
        this.tableName = metaData.getName();
        this.idFieldName = metaData.getIdField().getName();
        this.columnNames = metaData.getFieldsWithoutId()
                .stream()
                .map(Field::getName)
                .toList();
    }

    @Override
    public String getSelectAllSql() {
        return "SELECT * FROM " + tableName;
    }

    @Override
    public String getSelectByIdSql() {
        return String.format("SELECT * FROM %s WHERE %s = ?", tableName, idFieldName);
    }

    @Override
    public String getInsertSql() {
        String columns = String.join(", ", columnNames);
        String placeholders = String.join(", ", columnNames.stream().map(col -> "?").toList());
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
    }

    @Override
    public String getUpdateSql() {
        String assignments = columnNames.stream()
                .map(col -> col + " = ?")
                .collect(Collectors.joining(", "));
        return String.format("UPDATE %s SET %s WHERE %s = ?", tableName, assignments, idFieldName);
    }
}