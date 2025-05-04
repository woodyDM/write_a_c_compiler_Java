package cn.deepmax.jfx.parse;

public class AST {


    public static class Program implements AstNode {
        public final FunctionDefinition functionDef;

        public Program(FunctionDefinition functionDef) {
            this.functionDef = functionDef;
        }

        @Override
        public String toString() {
            return String.format("Program(%s)", functionDef.toString());
        }
    }

    public static class FunctionDefinition implements AstNode {
        public final String name;
        public final Statement body;

        public FunctionDefinition(String name, Statement body) {
            this.name = name;
            this.body = body;
        }

        @Override
        public String toString() {
            return String.format("Function(name=%s;body=%s)", name, body.toString());
        }
    }

    public static class Statement implements AstNode {
        public final Exp exp;

        public Statement(Exp exp) {
            this.exp = exp;
        }

        @Override
        public String toString() {
            return String.format("Statement(%s)", exp.toString());
        }
    }

    public static class Exp implements AstNode {
        public final int value;

        public Exp(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("Constant(%s)", value + "");
        }
    }

    public static class Identifier implements AstNode {
        public final String name;

        public Identifier(String name) {
            this.name = name;
        }
    }


}
