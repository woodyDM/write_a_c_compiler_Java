package cn.deepmax.jfx.ir;

import cn.deepmax.jfx.parse.Variables;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IRType {

    private IRType() {
    }

    public record Constant(int v) implements IR.Val {
    }

    public record Var(String identifier) implements IR.Val {
        public static Var makeTemp() {
            String name = makeTempVarName();
            return new Var(name);
        }

        public int getStackOffset() {
            int idx = identifier.indexOf(".");
            if (idx != -1) {
                int id = Integer.parseInt(identifier.substring(idx + 1));
                return id * 4;
            } else {
                throw new UnsupportedOperationException("identifier name not valid : " + identifier);
            }
        }

        private static String makeTempVarName() {
            return "var." + Variables.nextId();
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
    }


    public record FunctionDef(String identifier, List<IR.Instruction> body) implements IR.FunctionDef {
    }

    public record Program(IR.FunctionDef functionDef) implements IR.Program {

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
