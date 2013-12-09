package com.grootcode.android.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import android.text.TextUtils;

public class EntityBase<E extends EntityBase<E>> {

    /** Two space indentation */
    private static final String INDENTATION = "  ";

    @Override
    public String toString() {
        return composeToString(getClass());
    }

    protected String composeToString(Class<?> entityClass) {
        if (entityClass == null || entityClass == EntityBase.class)
            return "";

        Field[] fields = entityClass.getDeclaredFields();
        String className = entityClass.getSimpleName();

        StringBuilder message = new StringBuilder(className).append(":{\n");

        String superToString = composeToString(entityClass.getSuperclass());
        if (!TextUtils.isEmpty(superToString)) {
            for (String superToStringElement : superToString.split("\n")) {
                message.append(INDENTATION).append(superToStringElement).append("\n");
            }
        }

        int modifierToIgnore = Modifier.STATIC;

        for (Field field : fields) {
            /* if filed is not static */
            if ((field.getModifiers() & modifierToIgnore) == 0) {
                try {
                    field.setAccessible(true);
                    message.append(field.getName()).append('=').append(field.get(this)).append('\n');
                } catch (Exception unreachableExp) {
                    throw new RuntimeException("Unable to access " + className + ":" + field.getName(), unreachableExp);
                }
            }
        }

        message.append("}");

        return message.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return equals(getClass(), obj);
    }

    protected boolean equals(Class<?> entityClass, Object obj) {
        return equals(entityClass, obj, Modifier.STATIC | Modifier.TRANSIENT);
    }

    protected boolean equals(Class<?> entityClass, Object obj, int modifierToIgnore) {
        if (entityClass == null || obj == this || entityClass == EntityBase.class)
            return true;

        if (obj == null || !entityClass.isAssignableFrom(obj.getClass()))
            return false;

        boolean isEqual = equals(entityClass.getSuperclass(), obj, modifierToIgnore);

        Field[] fields = entityClass.getDeclaredFields();
        String className = entityClass.getSimpleName();

        for (Field field : fields) {
            /**
             * if filed is not modifier with modifiers in {@code modifierToIgnore}
             */
            if ((field.getModifiers() & modifierToIgnore) == 0) {
                try {
                    field.setAccessible(true);

                    Object value1 = field.get(this);
                    Object value2 = field.get(obj);

                    isEqual = isEqual && (value1 == null ? value2 == null : value1.equals(value2));
                } catch (Exception unreachableExp) {
                    throw new RuntimeException("Unable to access " + className + ":" + field.getName(), unreachableExp);
                }
            }
        }

        return isEqual;
    }

    public void update(E entity) {
        update(getClass(), entity, Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT);
    }

    protected void update(Class<?> entityClass, E entity, int modifierToIgnore) {
        if (entity == null || entityClass == null || entityClass == EntityBase.class
                || !entityClass.isAssignableFrom(entity.getClass()))
            return;

        update(entityClass.getSuperclass(), entity, modifierToIgnore);

        Field[] fields = entityClass.getDeclaredFields();
        String className = entityClass.getSimpleName();

        for (Field field : fields) {
            /* if filed is not final or static */
            if ((field.getModifiers() & modifierToIgnore) == 0) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    field.set(this, value);
                } catch (Exception unreachableExp) {
                    throw new RuntimeException(className + ":" + field.getName() + " field failed update",
                            unreachableExp);
                }
            }
        }
    }
}
