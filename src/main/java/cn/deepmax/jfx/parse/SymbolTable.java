package cn.deepmax.jfx.parse;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public static final SymbolTable TABLE = new SymbolTable();

    Map<String, TypeDef.Type> globalTable = new HashMap<>();

    public void put(String id, TypeDef.Type type) {
        globalTable.put(id, type);
    }

    public TypeDef.Type get(String id) {
        return globalTable.get(id);
    }

}
