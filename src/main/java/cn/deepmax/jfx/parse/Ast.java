package cn.deepmax.jfx.parse;

import java.util.List;


public class Ast {


    public record AstProgram(FunctionDefinition functionDefinition) implements AstNode.Program {

    }

    public record FunctionDefinition(String name, List<AstNode.BlockItem> body)
            implements AstNode.FunctionDef {

    }

    public record StatementBlockItem(AstNode.Statement statement) implements AstNode.BlockItem {
    }

    public record DeclareBlockItem(AstNode.Declaration statement) implements AstNode.BlockItem {
    }

    public record Expression(AstNode.Exp exp) implements AstNode.Statement {
    }

    public record Null() implements AstNode.Statement {
    }

    public record Declare(String identifier, AstNode.Exp exp) implements AstNode.Declaration {

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

    public record Var(String identifier) implements AstNode.Exp {
    }

    public record Assignment(AstNode.Exp left, AstNode.Exp right) implements AstNode.Exp {
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
        Remainder,
        //logic
        And,
        Or,
        Equal,
        NotEqual,
        LessThan,
        LessOrEqual,
        GreaterThan,
        GreaterOrEqual
    }

    public static AstNode.Exp unwrap(AstNode.Exp exp) {
        if (exp instanceof FactorExp(AstNode.Factor factor)) {
            if (factor instanceof ExpFactor(AstNode.Exp exp1)) {
                return exp1;
            }
        }
        return exp;
    }
}
