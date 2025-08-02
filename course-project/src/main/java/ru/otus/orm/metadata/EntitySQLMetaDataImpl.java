package ru.otus.orm.metadata;

import ru.otus.orm.annotations.Column;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {
    private final String selectAllSql;
    private final String selectByIdSql;
    private final String insertSql;
    private final String updateSql;

    public EntitySQLMetaDataImpl(EntityClassMetaData<?> metaData) {
        this.selectAllSql = buildSelectAllSql(metaData);
        this.selectByIdSql = buildSelectByIdSql(metaData);
        this.insertSql = buildInsertSql(metaData);
        this.updateSql = buildUpdateSql(metaData);
    }

    private String buildSelectAllSql(EntityClassMetaData<?> metaData) {
        return String.format("SELECT * FROM %s", metaData.getTableName());
    }

    private String buildSelectByIdSql(EntityClassMetaData<?> metaData) {
        return String.format("SELECT * FROM %s WHERE %s = ?",
                metaData.getTableName(),
                getColumnName(metaData.getIdField()));
    }

    private String buildInsertSql(EntityClassMetaData<?> metaData) {
        String columns = metaData.getFieldsWithoutId().stream()
                .map(this::getColumnName)
                .collect(Collectors.joining(", "));

        String values = metaData.getFieldsWithoutId().stream()
                .map(f -> "?")
                .collect(Collectors.joining(", "));

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                metaData.getTableName(),
                columns,
                values);
    }

    private String buildUpdateSql(EntityClassMetaData<?> metaData) {
        String setClause = metaData.getFieldsWithoutId().stream()
                .map(f -> String.format("%s = ?", getColumnName(f)))
                .collect(Collectors.joining(", "));

        return String.format("UPDATE %s SET %s WHERE %s = ?",
                metaData.getTableName(),
                setClause,
                getColumnName(metaData.getIdField()));
    }

    private String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column != null && !column.name().isEmpty()
                ? column.name()
                : field.getName();
    }

    @Override
    public String getSelectAllSql() {
        return selectAllSql;
    }

    @Override
    public String getSelectByIdSql() {
        return selectByIdSql;
    }

    @Override
    public String getInsertSql() {
        return insertSql;
    }

    @Override
    public String getUpdateSql() {
        return updateSql;
    }
}