package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.utils.Nullable;

import java.util.List;

public class Ast {


    public record AstProgram(List<FunctionDeclare> functionDeclarations) implements AstNode.Program {

    }

    public record StatementBlockItem(AstNode.Statement statement) implements AstNode.BlockItem {
    }

    public record DeclareBlockItem(AstNode.Declaration statement) implements AstNode.BlockItem {
    }

    public record If(AstNode.Exp condition, AstNode.Statement then, @Nullable AstNode.Statement elseSt)
            implements AstNode.Statement {
    }

    public record Expression(AstNode.Exp exp) implements AstNode.Statement {
    }

    public record Null() implements AstNode.Statement {
    }

    public record AnnotationLabeledStatement(AstNode.Statement statement, String label) implements AstNode.Statement {

    }

    public record Break() implements AstNode.Statement {
    }

    public record BreakLabel(String label) implements AstNode.Statement {
    }

    public record ContinueLabel(String label) implements AstNode.Statement {
    }

    public record Continue() implements AstNode.Statement {
    }

    public record While(AstNode.Exp condition, AstNode.Statement body) implements AstNode.Statement {
    }

    public record DoWhile(AstNode.Statement body, AstNode.Exp condition) implements AstNode.Statement {
    }

    public record For(AstNode.ForInit init, @Nullable AstNode.Exp condition, @Nullable AstNode.Exp post,
                      AstNode.Statement body) implements AstNode.Statement {
    }

    public record ForInitDeclare(AstNode.Declaration declaration) implements AstNode.ForInit {
    }

    public record ForInitExp(@Nullable AstNode.Exp exp) implements AstNode.ForInit {
    }

    public record Block(List<AstNode.BlockItem> blockItems) {

    }

    public record VarParam(String type, @Nullable String identifier) implements AstNode.Param {
    }

    public record ArgumentList(List<AstNode.Exp> expList) {
    }

    public record Compound(Block block) implements AstNode.Statement {

    }

    public record VarDeclare(String identifier, @Nullable AstNode.Exp exp) implements AstNode.Declaration {

    }

    public record FunctionDeclare(String identifier, List<AstNode.Param> params, @Nullable Block body)
            implements AstNode.Declaration {

        public boolean sameParamDef(FunctionDeclare other) {
            return other.realParamSize() == this.realParamSize();
        }

        public int realParamSize() {
            return params.size();
        }
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

    public record Conditional(AstNode.Exp condition, AstNode.Exp trueExp, AstNode.Exp falseExp)
            implements AstNode.Exp {

    }

    public record FunctionCall(String identifier, List<AstNode.Exp> args) implements AstNode.Factor {

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
