package cn.deepmax.jfx.parse;

import java.util.ArrayList;
import java.util.List;

public class TypeDef {


    private TypeDef() {
    }

    public final static class FunType implements Type {
        public final int paramCount;
        public final boolean defined;
        public final List<String> localVariables = new ArrayList<>();

        public FunType(int paramCount, boolean defined) {
            this.paramCount = paramCount;
            this.defined = defined;
        }

        public void addVariables(String id) {
            if (!localVariables.contains(id)) {
                localVariables.add(id);
            }
        }

        public static FunType newInstanceFrom(int paramCount, boolean defined, Type other) {

            FunType result = new FunType(paramCount, defined);
            if (other == null) return result;
            if (other instanceof FunType funType) {
                if (funType.localVariables.isEmpty()) return result;
                result.localVariables.addAll(funType.localVariables);
                return result;
            } else {
                throw new IllegalStateException("type is not funcType " + other);
            }
        }

    }


    enum VariableType implements Type {
        Int,
    }

    public sealed interface Type permits VariableType, FunType {

    }

}
