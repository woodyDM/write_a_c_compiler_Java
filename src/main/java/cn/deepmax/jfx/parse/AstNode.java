package cn.deepmax.jfx.parse;

public interface AstNode {

    interface Program {
    }

    interface BlockItem {

    }

    /**
     * 函数参数列表
     */
    sealed interface Param permits Ast.VarParam {

    }

    interface Argument {

    }

    interface Statement {
    }

    sealed interface Declaration permits Ast.FunctionDeclare, Ast.VarDeclare {
    }

    interface Exp {
    }

    interface Factor {

    }

    sealed interface ForInit permits Ast.ForInitDeclare, Ast.ForInitExp {

    }

    interface UnaryOperator {
    }

    interface BinaryOperator {
    }
}
