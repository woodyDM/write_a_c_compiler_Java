package cn.deepmax.jfx.parse;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public static final SymbolTable TABLE = new SymbolTable();

    Map<String, TypeDef.Type> globalTable = new HashMap<>();

    public void put(String id, TypeDef.Type type) {
        globalTable.put(id, type);
    }

    public void putVariable(String funcName, String id, TypeDef.Type type) {
        mustGetFunc(funcName).addVariables(id);
        globalTable.put(id, type);
    }

    public TypeDef.FunType mustGetFunc(String funcName) {
        TypeDef.Type fn = globalTable.get(funcName);
        TypeDef.FunType fun = (TypeDef.FunType) fn;
        return fun;
    }

    public TypeDef.Type get(String id) {
        return globalTable.get(id);
    }

}
