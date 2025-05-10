package cn.deepmax.jfx.ir;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IRType {

    private IRType() {
    }

    public record Complement() implements IR.UnaryOperator {
    }

    public record Negate() implements IR.UnaryOperator {
    }

    public record Constant(int v) implements IR.Val {
    }

    public record Var(String identifier) implements IR.Val {
        public static AtomicInteger varId = new AtomicInteger(0);

        public static Var makeTemp() {
            String name = makeTempVarName();
            return new Var(name);
        }

        public int getStackOffset() {
            if (identifier.startsWith("var.")) {
                int idx = identifier.indexOf(".");
                int id = Integer.parseInt(identifier.substring(idx + 1));
                return id * 4;
            } else {
                throw new UnsupportedOperationException("id not valid " + identifier);
            }
        }

        private static String makeTempVarName() {
            return "var." + varId.incrementAndGet();
        }
    }

    public record Return(IR.Val value) implements IR.Instruction {
    }

    public record Unary(IR.UnaryOperator op, IR.Val src, IR.Val dst) implements IR.Instruction {
    }

    public record FunctionDef(String identifier, List<IR.Instruction> body) implements IR.FunctionDef {
    }

    public record Program(IR.FunctionDef functionDef) implements IR.Program {

    }
}
