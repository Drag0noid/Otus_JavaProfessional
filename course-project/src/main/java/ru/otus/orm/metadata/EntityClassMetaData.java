package ru.otus.orm.metadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public interface EntityClassMetaData<T> {
    String getName();
    String getTableName();
    Field getIdField();
    List<Field> getAllFields();
    List<Field> getFieldsWithoutId();
    Constructor<T> getConstructor() throws NoSuchMethodException;
}