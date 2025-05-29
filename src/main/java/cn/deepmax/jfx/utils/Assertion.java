package cn.deepmax.jfx.utils;

public class Assertion {

    public static void notNull(Object object) {
        if (object == null) {
            throw new IllegalStateException("object is null!");
        }
    }
}
