package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.exception.SemanticException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * handle variable declare and scope
 */
public class Identifiers {

    private final Map<String, FunEntry> funMap = new HashMap<>();
    private final Map<String, VarEntry> varMap = new HashMap<>();

    Identifiers parent;
    private static final AtomicLong seq = new AtomicLong(0);

    public String mappingToReplacement(String rawId) {
        var entry = varMap.get(rawId);
        return entry == null ? null : entry.replacedName;
    }

    /**
     * resolve local variable
     *
     * @param idValue
     * @param storageClass
     * @return
     */
    public String handleLocalVariable(String idValue, AstNode.StorageClass storageClass) {
        VarEntry varEntry = varMap.get(idValue);
        if (varEntry != null && varEntry.currentScope) {
            if (!(varEntry.hasLinkage()) && Objects.equals(storageClass, AstNode.StorageClass.Extern)) {
                throw new SemanticException("Duplicate variable declaration! id = " + idValue);
            }
        }
        FunEntry funEntry = funMap.get(idValue);
        if (funEntry != null && funEntry.currentScope) {
            throw new SemanticException("Id redeclared as different kind of symbol! id = " + idValue);
        }
        //put
        String replacedName = idValue + "." + Identifiers.nextId();
        varMap.put(idValue, new VarEntry(replacedName, true, false));
        return replacedName;
    }


    public String putFileVar(String identifier, boolean currentBlock) {
        varMap.put(identifier, new VarEntry(identifier, currentBlock, true));
        return identifier;
    }

    public void putFunc(String identifier, Ast.FunctionDeclare fn) {
        VarEntry varEntry = varMap.get(identifier);
        if (varEntry != null && varEntry.currentScope) {
            throw new SemanticException("Variable redeclared as function!" + identifier);
        }
        funMap.put(identifier, new FunEntry(true, true, fn));
    }

    public Identifiers newScope() {
        Identifiers r = new Identifiers();
        funMap.forEach((k, v) -> r.funMap.put(k, new FunEntry(false, v.hasLinkage, v.functionDeclare)));
        varMap.forEach((k, v) -> r.varMap.put(k, new VarEntry(v.replacedName, false, v.hasLinkage)));
        r.parent = this;
        return r;
    }


    public void checkFunCallName(String identifier) {
        var en = varMap.get(identifier);
        if (en != null && en.currentScope) {
            throw new SemanticException("Variable used as function name " + identifier);
        }
    }

    private static long nextId() {
        return seq.getAndIncrement();
    }


    public record VarEntry(String replacedName, boolean currentScope, boolean hasLinkage) {

    }

    public record FunEntry(boolean currentScope,
                           boolean hasLinkage, Ast.FunctionDeclare functionDeclare) {

    }
}
