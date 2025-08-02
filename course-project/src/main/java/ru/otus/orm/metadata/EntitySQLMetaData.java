package ru.otus.orm.metadata;

public interface EntitySQLMetaData {
    String getSelectAllSql();
    String getSelectByIdSql();
    String getInsertSql();
    String getUpdateSql();
}