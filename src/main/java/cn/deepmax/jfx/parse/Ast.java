package cn.deepmax.jfx.parse;

public class Ast {


    public static class AstProgram implements AstNode.Program {
        public final FunctionDefinition functionDef;

        public AstProgram(FunctionDefinition functionDef) {
            this.functionDef = functionDef;
        }

        @Override
        public String toString() {
            return String.format("Program(%s)", functionDef.toString());
        }
    }

    public static class FunctionDefinition implements AstNode.FunctionDef {
        public final String name;
        public final AstNode.Statement body;

        public FunctionDefinition(String name, ReturnStatement body) {
            this.name = name;
            this.body = body;
        }

        @Override
        public String toString() {
            return String.format("Function(name=%s;body=%s)", name, body.toString());
        }
    }

    public static class ReturnStatement implements AstNode.Statement {
        public final AstNode.Exp intExp;

        public ReturnStatement(IntExp intExp) {
            this.intExp = intExp;
        }

        @Override
        public String toString() {
            return String.format("Statement(%s)", intExp.toString());
        }
    }

    public static class IntExp implements AstNode.Exp {
        public final int value;

        public IntExp(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("Constant(%s)", value + "");
        }
    }


}
