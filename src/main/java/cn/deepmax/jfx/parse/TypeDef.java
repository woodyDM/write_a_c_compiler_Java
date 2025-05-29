package cn.deepmax.jfx.parse;

public class TypeDef {

    record FunType(int paramCount, boolean defined) implements Type {

    }

    enum VariableType implements Type {
        Int,
    }

    public sealed interface Type permits VariableType, FunType {

    }

}
