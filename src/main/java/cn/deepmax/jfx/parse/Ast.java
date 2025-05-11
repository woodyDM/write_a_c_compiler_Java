package cn.deepmax.jfx.parse;

public class Ast {


    public record AstProgram(FunctionDefinition functionDefinition) implements AstNode.Program {

    }

    public record FunctionDefinition(String name, AstNode.Statement body)
            implements AstNode.FunctionDef {

    }

    public record ReturnStatement(AstNode.Exp exp)
            implements AstNode.Statement {
    }

    public record IntConstantFactor(int value)
            implements AstNode.Factor {
    }

    public record FactorExp(AstNode.Factor factor) implements AstNode.Exp {

    }

    public record ExpFactor(AstNode.Exp exp) implements AstNode.Factor {

    }

    public record Unary(AstNode.UnaryOperator operator, AstNode.Factor factor)
            implements AstNode.Factor {
    }

    public record Binary(AstNode.BinaryOperator operator, AstNode.Exp left, AstNode.Exp right)
            implements AstNode.Exp {

    }

    public enum UnaryOp implements AstNode.UnaryOperator {
        Complement,
        Negate,
        Not
    }

    public enum BinaryOp implements AstNode.BinaryOperator {
        Add,
        Subtract,
        Multiply,
        Divide,
        Remainder
    }

}
