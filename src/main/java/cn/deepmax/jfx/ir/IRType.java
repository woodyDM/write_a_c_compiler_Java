package cn.deepmax.jfx.ir;

import java.util.List;

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
