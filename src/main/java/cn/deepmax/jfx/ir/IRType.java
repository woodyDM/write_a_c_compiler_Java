package cn.deepmax.jfx.ir;

import cn.deepmax.jfx.parse.Identifiers;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class IRType {

    private IRType() {
    }

    public record Constant(int v) implements IR.Val {

    }

    public record Var(String identifier) implements IR.Val {

        private static final AtomicLong id = new AtomicLong(0);

        public static Var makeTemp() {
            String name = makeTempVarName();
            return new Var(name);
        }

        public static long idOf(String identifier) {
            int idx = identifier.indexOf(".");
            if (idx != -1) {
                long id = Long.parseLong(identifier.substring(idx + 1));
                return id;
            } else {
                throw new UnsupportedOperationException("identifier name not valid : " + identifier);
            }
        }

        public static int offset(String identifier) {
            return (int) (idOf(identifier) * 4);
        }

        public int getStackOffset() {
            return offset(this.identifier());
        }

        public static long currentId() {
            return id.get();
        }

        private static String makeTempVarName() {
            return "var." + id.getAndIncrement();
        }
    }

    public record Return(IR.Val value) implements IR.Instruction {
    }

    public record Unary(IR.UnaryOperator op, IR.Val src, IR.Val dst) implements IR.Instruction {
    }

    public record Binary(IR.BinaryOperator op, IR.Val src1, IR.Val src2, IR.Val dst) implements IR.Instruction {

    }

    public record Copy(IR.Val src, IR.Val dst) implements IR.Instruction {
    }

    public record Jump(String targetIdentifier) implements IR.Instruction {
    }

    public record JumpIfZero(IR.Val condition, String target) implements IR.Instruction {
    }

    public record JumpIfNotZero(IR.Val condition, String target) implements IR.Instruction {
    }

    public record Label(String identifier) implements IR.Instruction {
        public static AtomicInteger id = new AtomicInteger(0);

        public static int nextId() {
            return id.getAndIncrement();
        }
    }

    public record FunCall(String functionName, List<IR.Val> args, IR.Val dst) implements IR.Instruction {

    }

    public record FunctionDef(String identifier, List<String> params,
                              List<IR.Instruction> body) implements IR.FunctionDef {
    }

    public record Program(List<IR.FunctionDef> functionDef) implements IR.Program {

    }

    public enum UnaryOp implements IR.UnaryOperator {
        Complement,
        Negate,
        Not
    }

    public enum BinaryOp implements IR.BinaryOperator {
        Add,
        Subtract,
        Multiply,
        Divide,
        Remainder,

        And,
        Or,

        Equal,
        NotEqual,
        LessThan,
        LessOrEqual,
        GreaterThan,
        GreaterOrEqual,

        ;

        public boolean isLogic() {
            return this == Equal || this == NotEqual ||
                    this == LessThan || this == LessOrEqual ||
                    this == GreaterThan || this == GreaterOrEqual;
        }
    }

}
