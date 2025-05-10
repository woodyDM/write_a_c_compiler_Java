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

    public record UnaryOpComplement() implements AstNode.UnaryOperator {
    }

    public record UnaryOpNegate() implements AstNode.UnaryOperator {
    }

    public record Add() implements AstNode.BinaryOperator {

    }

    public record Subtract() implements AstNode.BinaryOperator {
    }

    public record Multiply() implements AstNode.BinaryOperator {
    }

    public record Divide() implements AstNode.BinaryOperator {
    }

    public record Remainder() implements AstNode.BinaryOperator {
    }

}
