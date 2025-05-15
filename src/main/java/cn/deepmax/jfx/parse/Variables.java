package cn.deepmax.jfx.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Variables {

    Map<String, String> variables = new HashMap<>();
    static final AtomicLong seq = new AtomicLong(0);

    public boolean exist(String rawId) {
        return variables.containsKey(rawId);
    }

    public static int currentNumber() {
        return (int) seq.get();
    }

    public String get(String rawId) {
        return variables.get(rawId);
    }

    public static long nextId() {
        return seq.getAndIncrement();
    }

    public void put(String identifier, String name) {
        String old = variables.put(identifier, name);
        if (old != null) {
            throw new IllegalStateException();
        }
    }
}
