package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.exception.SemanticException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * handle variable declare and scope
 */
public class Identifiers {

    Map<String, Entry> funMap = new HashMap<>();
    Map<String, VarEntry> varMap = new HashMap<>();

    Identifiers parent;
    static final AtomicLong seq = new AtomicLong(0);

    public void checkVar(String varRawId) {
        VarEntry varEntry = varMap.get(varRawId);
        if (varEntry != null && varEntry.currentScope) {
            throw new SemanticException("Duplicate variable declaration! id = " + varRawId);
        }
        Entry funEntry = funMap.get(varRawId);
        if (funEntry != null && funEntry.currentScope) {
            throw new SemanticException("Id redeclared as different kind of symbol! id = " + varRawId);
        }
    }

    public static int currentNumber() {
        return (int) seq.get();
    }

    public String mappingToReplacement(String rawId) {
        var entry = varMap.get(rawId);
        return entry == null ? null : entry.replacedName;
    }

    public static long nextId() {
        return seq.getAndIncrement();
    }

    public String putVar(String identifier, boolean currentBlock) {
        String replacedName = identifier + "." + Identifiers.nextId();
        varMap.put(identifier, new VarEntry(replacedName, currentBlock));
        return replacedName;
    }

    public void putFunc(String identifier, Ast.FunctionDeclare fn) {
        funMap.put(identifier, new Entry(true, true, fn));
    }

    public Identifiers newScope() {
        Identifiers r = new Identifiers();
        funMap.forEach((k, v) -> r.funMap.put(k, new Entry(false, v.hasLinkage, v.functionDeclare)));
        varMap.forEach((k, v) -> r.varMap.put(k, new VarEntry(v.replacedName, false)));
        r.parent = this;
        return r;
    }

    public record VarEntry(String replacedName, boolean currentScope) {

    }

    public record Entry(boolean currentScope,
                        boolean hasLinkage, Ast.FunctionDeclare functionDeclare) {

    }
}
