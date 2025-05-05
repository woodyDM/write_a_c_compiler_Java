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

    public record IntExp(int value)
            implements AstNode.Exp {
    }

    public record Unary(AstNode.UnaryOperator operator, AstNode.Exp exp)
            implements AstNode.Exp {
    }

    public record UnaryOpComplement() implements AstNode.UnaryOperator {
    }

    public record UnaryOpNegate() implements AstNode.UnaryOperator {
    }

}
