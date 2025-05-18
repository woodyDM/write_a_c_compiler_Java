package cn.deepmax.jfx.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * handle variable declare and scope
 */
public class Variables {

    Map<String, Entry> variables = new HashMap<>();
    Variables parent;
    static final AtomicLong seq = new AtomicLong(0);

    public boolean existInCurrentScope(String rawId) {
        if (variables.containsKey(rawId)) {
            return variables.get(rawId).currentBlock;
        }
        return false;
    }

    public static int currentNumber() {
        return (int) seq.get();
    }

    public String mappingToReplacement(String rawId) {
        var entry = variables.get(rawId);
        return entry == null ? null : entry.replacedName;
    }

    public static long nextId() {
        return seq.getAndIncrement();
    }

    public void put(String identifier, String replacedName, boolean currentBlock) {
        variables.put(identifier, new Entry(replacedName, currentBlock));
    }

    public Variables newScope() {
        Variables r = new Variables();
        variables.forEach((k, v) -> r.variables.put(k, new Entry(v.replacedName, false)));
        r.parent = this;
        return r;
    }

    public record Entry(String replacedName, boolean currentBlock) {

    }
}
