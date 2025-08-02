package ru.otus.orm.metadata;

import ru.otus.orm.annotations.Entity;
import ru.otus.orm.annotations.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {
    private final Class<T> clazz;
    private String tableName;
    private Field idField;
    private List<Field> allFields;
    private List<Field> fieldsWithoutId;
    private Constructor<T> constructor;

    public EntityClassMetaDataImpl(Class<T> clazz) {
        this.clazz = clazz;
        parseClass();
    }

    private void parseClass() {
        // Парсинг аннотации @Entity
        Entity entity = clazz.getAnnotation(Entity.class);
        this.tableName = entity != null && !entity.table().isEmpty()
                ? entity.table()
                : clazz.getSimpleName().toLowerCase();

        // Поиск полей
        this.allFields = List.of(clazz.getDeclaredFields());
        this.fieldsWithoutId = new ArrayList<>();
        this.idField = null;

        for (Field field : allFields) {
            if (field.isAnnotationPresent(Id.class)) {
                this.idField = field;
            } else {
                this.fieldsWithoutId.add(field);
            }
        }

        if (this.idField == null) {
            throw new IllegalStateException("No @Id field found in " + clazz.getName());
        }

        // Получаем конструктор по умолчанию
        try {
            this.constructor = clazz.getDeclaredConstructor();
            this.constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default constructor found in " + clazz.getName(), e);
        }
    }

    @Override
    public Constructor<T> getConstructor() {
        return constructor;
    }

    // Остальные методы интерфейса...
    @Override public String getName() { return clazz.getSimpleName(); }
    @Override public String getTableName() { return tableName; }
    @Override public Field getIdField() { return idField; }
    @Override public List<Field> getAllFields() { return allFields; }
    @Override public List<Field> getFieldsWithoutId() { return fieldsWithoutId; }
}