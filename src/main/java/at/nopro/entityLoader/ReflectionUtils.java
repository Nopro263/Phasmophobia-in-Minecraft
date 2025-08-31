package at.nopro.entityLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ReflectionUtils {
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> l = new ArrayList<>();
        while (clazz != Object.class) {
            l.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return l;
    }

    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> l = new ArrayList<>();
        while (clazz != Object.class) {
            l.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            clazz = clazz.getSuperclass();
        }
        return l;
    }
}
