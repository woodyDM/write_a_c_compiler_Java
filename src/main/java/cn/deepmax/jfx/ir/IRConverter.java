package cn.deepmax.jfx.ir;

import cn.deepmax.jfx.parse.Ast;
import cn.deepmax.jfx.parse.AstNode;

import java.util.ArrayList;
import java.util.List;

public class IRConverter {

    private AstNode.Program program;

    public IRConverter(AstNode.Program program) {
        this.program = program;
    }

    public IR.Program convertToIR() {
        return new IRType.Program(convertFn());
    }

    private IR.FunctionDef convertFn() {
        var fn = ((Ast.AstProgram) program).functionDefinition();
        return new IRType.FunctionDef(fn.name(), convertIns(fn.body()));
    }

    private List<IR.Instruction> convertIns(AstNode.Statement statement) {
        List<IR.Instruction> list = new ArrayList<>();
        switch (statement) {
            case Ast.ReturnStatement rs -> {
                AstNode.Exp exp = rs.exp();
                IRType.Return rt = new IRType.Return(convertValue(exp, list));
                list.add(rt);
            }
            default -> throw new UnsupportedOperationException(statement.toString());
        }
        return list;
    }

    private IR.Val convertValue(AstNode.Exp exp, List<IR.Instruction> list) {
        return switch (exp) {
            case Ast.FactorExp f -> switch (f.factor()) {
                case Ast.IntConstantFactor i -> new IRType.Constant(i.value());
                case Ast.Unary u -> {
                    var src = convertValue(new Ast.FactorExp(u.factor()), list);
                    var dst = IRType.Var.makeTemp();
                    var op = convertUnaryOp(u);
                    list.add(new IRType.Unary(op, src, dst));
                    yield dst;
                }
                case Ast.ExpFactor e -> convertValue(e.exp(), list);
                default -> throw new UnsupportedOperationException("invalid factor " + f.factor().toString());
            };
            case Ast.Binary b -> {
                var bop = b.operator();
                if (bop == Ast.BinaryOp.And) {
                    //should support short-circuit
                    String falseLabel = "and_false_label." + IRType.Label.id.getAndIncrement();
                    String exitLabel = "and_exit_label." + IRType.Label.id.getAndIncrement();
                    var dst = IRType.Var.makeTemp();

                    var v1 = convertValue(b.left(), list);
                    list.add(new IRType.JumpIfZero(v1, falseLabel));
                    var v2 = convertValue(b.right(), list);
                    list.add(new IRType.JumpIfZero(v2, falseLabel));
                    list.add(new IRType.Copy(new IRType.Constant(1), dst));
                    list.add(new IRType.Jump(exitLabel));
                    list.add(new IRType.Label(falseLabel));
                    list.add(new IRType.Copy(new IRType.Constant(0), dst));
                    list.add(new IRType.Label(exitLabel));

                    yield dst;
                } else if (bop == Ast.BinaryOp.Or) {
                    //should support short-circuit
                    String trueLabel = "or_true_label." + IRType.Label.id.getAndIncrement();
                    String exitLabel = "or_exit_label." + IRType.Label.id.getAndIncrement();
                    var dst = IRType.Var.makeTemp();

                    var v1 = convertValue(b.left(), list);
                    list.add(new IRType.JumpIfNotZero(v1, trueLabel));
                    var v2 = convertValue(b.right(), list);
                    list.add(new IRType.JumpIfNotZero(v2, trueLabel));
                    list.add(new IRType.Copy(new IRType.Constant(0), dst));
                    list.add(new IRType.Jump(exitLabel));
                    list.add(new IRType.Label(trueLabel));
                    list.add(new IRType.Copy(new IRType.Constant(1), dst));
                    list.add(new IRType.Label(exitLabel));

                    yield dst;
                } else {
                    //normal operators
                    var v1 = convertValue(b.left(), list);
                    var v2 = convertValue(b.right(), list);
                    var dst = IRType.Var.makeTemp();
                    IR.BinaryOperator op = convertBinaryOp(b.operator());
                    list.add(new IRType.Binary(op, v1, v2, dst));
                    yield dst;
                }

            }
            default -> throw new UnsupportedOperationException(exp.toString());
        };
    }

    private IR.BinaryOperator convertBinaryOp(AstNode.BinaryOperator operator) {
        return switch (operator) {
            case Ast.BinaryOp.Add -> IRType.BinaryOp.Add;
            case Ast.BinaryOp.Subtract -> IRType.BinaryOp.Subtract;
            case Ast.BinaryOp.Multiply -> IRType.BinaryOp.Multiply;
            case Ast.BinaryOp.Divide -> IRType.BinaryOp.Divide;
            case Ast.BinaryOp.Remainder -> IRType.BinaryOp.Remainder;

            case Ast.BinaryOp.Equal -> IRType.BinaryOp.Equal;
            case Ast.BinaryOp.NotEqual -> IRType.BinaryOp.NotEqual;
            case Ast.BinaryOp.And -> IRType.BinaryOp.And;
            case Ast.BinaryOp.Or -> IRType.BinaryOp.Or;
            case Ast.BinaryOp.LessThan -> IRType.BinaryOp.LessThan;
            case Ast.BinaryOp.LessOrEqual -> IRType.BinaryOp.LessOrEqual;
            case Ast.BinaryOp.GreaterThan -> IRType.BinaryOp.GreaterThan;
            case Ast.BinaryOp.GreaterOrEqual -> IRType.BinaryOp.GreaterOrEqual;
            default -> throw new UnsupportedOperationException("invalid binary op " + operator);
        };
    }

    private IR.UnaryOperator convertUnaryOp(Ast.Unary u) {
        return switch (u.operator()) {
            case Ast.UnaryOp.Complement -> IRType.UnaryOp.Complement;
            case Ast.UnaryOp.Negate -> IRType.UnaryOp.Negate;
            case Ast.UnaryOp.Not -> IRType.UnaryOp.Not;
            default -> throw new UnsupportedOperationException(u.toString());
        };
    }

}
