package com.asd.redis.orm.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 反射工具类
 */
public class ReflectionUtils {

    /**
     * 获取类的所有字段（包括父类）
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * 查找带有指定注解的字段
     */
    public static Field findFieldWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Field> fields = getAllFields(clazz);

        for (Field field : fields) {
            if (field.isAnnotationPresent(annotationClass)) {
                return field;
            }
        }

        return null;
    }

    /**
     * 查找所有带有指定注解的字段
     */
    public static List<Field> findFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Field> fields = getAllFields(clazz);

        return fields.stream()
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .collect(Collectors.toList());
    }

    /**
     * 获取字段值
     */
    public static Object getFieldValue(Object obj, Field field) throws IllegalAccessException {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);
            return field.get(obj);
        } finally {
            field.setAccessible(accessible);
        }
    }

    /**
     * 获取对象字段的值
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @return 字段值
     * @throws IllegalAccessException 如果字段不可访问
     * @throws NoSuchFieldException   如果字段不存在
     */
    public static Object getFieldValue(Object obj, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        if (obj == null) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        Field field = null;

        // 尝试获取当前类的字段
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 如果当前类没有该字段，尝试从父类获取
            Class<?> superClass = clazz.getSuperclass();
            while (superClass != null) {
                try {
                    field = superClass.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException ex) {
                    superClass = superClass.getSuperclass();
                }
            }

            if (field == null) {
                throw new NoSuchFieldException("Field '" + fieldName + "' not found in class " + clazz.getName());
            }
        }

        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * 设置字段值
     */
    public static void setFieldValue(Object obj, Field field, Object value) throws IllegalAccessException {
        boolean accessible = field.isAccessible();
        try {
            field.setAccessible(true);

            // 处理基本类型转换
            if (value != null) {
                Class<?> fieldType = field.getType();
                if (fieldType.isPrimitive()) {
                    if (fieldType == int.class && value instanceof Number) {
                        field.setInt(obj, ((Number) value).intValue());
                        return;
                    } else if (fieldType == long.class && value instanceof Number) {
                        field.setLong(obj, ((Number) value).longValue());
                        return;
                    } else if (fieldType == double.class && value instanceof Number) {
                        field.setDouble(obj, ((Number) value).doubleValue());
                        return;
                    } else if (fieldType == float.class && value instanceof Number) {
                        field.setFloat(obj, ((Number) value).floatValue());
                        return;
                    } else if (fieldType == boolean.class && value instanceof Boolean) {
                        field.setBoolean(obj, (Boolean) value);
                        return;
                    } else if (fieldType == byte.class && value instanceof Number) {
                        field.setByte(obj, ((Number) value).byteValue());
                        return;
                    } else if (fieldType == short.class && value instanceof Number) {
                        field.setShort(obj, ((Number) value).shortValue());
                        return;
                    } else if (fieldType == char.class && value instanceof Character) {
                        field.setChar(obj, (Character) value);
                        return;
                    }
                }
            }

            field.set(obj, value);
        } finally {
            field.setAccessible(accessible);
        }
    }

    /**
     * 获取对象的非空字段及其值
     */
    public static Map<String, Object> getNonNullFields(Object obj) {
        Map<String, Object> result = new HashMap<>();
        if (obj == null) {
            return result;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    result.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }

        return result;
    }
}