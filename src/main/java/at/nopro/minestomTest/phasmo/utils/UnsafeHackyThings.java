package at.nopro.minestomTest.phasmo.utils;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class UnsafeHackyThings {
    private static Unsafe unsafe;

    static{
        try{
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    protected static void setFinal(Object base, long offset, Object value) {
        Class<?> type = value.getClass();
        switch (type.getSimpleName().toLowerCase()) {
            case "byte": unsafe.putByte(base, offset, (Byte) value); break;
            case "short": unsafe.putShort(base, offset, (Short) value); break;
            case "integer": unsafe.putInt(base, offset, (Integer) value); break;
            case "long": unsafe.putLong(base, offset, (Long) value); break;
            case "float": unsafe.putFloat(base, offset, (Float) value); break;
            case "double": unsafe.putDouble(base, offset, (Double) value); break;
            case "boolean": unsafe.putBoolean(base, offset, (Boolean) value); break;
            case "character": unsafe.putChar(base, offset, (Character) value); break;
            default: unsafe.putObject(base, offset, value);
        }

    }

    protected static <T> T getFinal(Object base, long offset, Class<T> type) {
        switch (type.getCanonicalName()) {
            case "byte": return (T) (Object) unsafe.getByte(base, offset);
            case "short": return (T) (Object) unsafe.getShort(base, offset);
            case "int": return (T) (Object) unsafe.getInt(base, offset);
            case "long": return (T) (Object) unsafe.getLong(base, offset);
            case "float": return (T) (Object) unsafe.getFloat(base, offset);
            case "double": return (T) (Object) unsafe.getDouble(base, offset);
            case "boolean": return (T) (Object) unsafe.getBoolean(base, offset);
            case "char": return (T) (Object) unsafe.getChar(base, offset);
        }
        return (T) unsafe.getObject(base, offset);
    }

    public static void set(Class<?> clazz, Object clazzObject, String name, Object o) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        if(clazzObject == null) {
            setFinal(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), o);
        } else {
            setFinal(clazzObject, unsafe.objectFieldOffset(field), o);
        }
    }

    public static <T> T get(Class<?> clazz, Object clazzObject, String name, Class<T> type) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        if(clazzObject == null) {
            return getFinal(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), type);
        } else {
            return getFinal(clazzObject, unsafe.objectFieldOffset(field), type);
        }
    }

    public static void set(Object clazzObject, String name, Object o) throws NoSuchFieldException {
        set(clazzObject.getClass(), clazzObject, name, o);
    }

    public static <T> T get(Object clazzObject, String name, Class<T> type) throws NoSuchFieldException {
        return get(clazzObject.getClass(), clazzObject, name, type);
    }
}
