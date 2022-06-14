package io.github.kvbc.endlessdragon;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtil {
    public static Method getPrivateMethod (Class cls, String name, Class<?>... parameterTypes) {
        try {
            Method m = cls.getDeclaredMethod(name, parameterTypes);
            m.setAccessible(true);
            return m;
        } catch (Exception e) {throw new RuntimeException(e); }
    }

    public static Object getPrivateField (Object obj, String name) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
