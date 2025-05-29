package cn.deepmax.jfx.ir;

public interface IR {
    interface Program {
    }

    sealed interface FunctionDef permits IRType.FunctionDef {
    }

    interface Instruction {
    }

    interface Val {
    }

    interface UnaryOperator {
    }

    interface BinaryOperator{

    }
}
