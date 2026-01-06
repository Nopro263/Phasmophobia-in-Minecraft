package at.nopro.phasmo.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class Reflection {
    public static List<Field> getAllDeclared(Object o) {
        return Arrays.stream(o.getClass().getDeclaredFields()).peek(f -> f.setAccessible(true)).toList();
    }

    public static <T> T get(Field f, Object o) {
        try {
            f.setAccessible(true);
            return (T) f.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void set(Field f, Object o, T v) {
        try {
            f.setAccessible(true);
            f.set(o, v);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T get(Object o, String name) {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public static <T> T getStatic(Object o, String name) {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public static void set(Object o, String name, Object v) {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(o, v);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {

        }
    }

    public static void setStatic(Object o, String name, Object v) {
        try {
            Field f = o.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(null, v);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {

        }
    }
}
