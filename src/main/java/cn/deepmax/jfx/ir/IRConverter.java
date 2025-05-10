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
            case Ast.IntExp ip -> new IRType.Constant(ip.value());
            case Ast.Unary u -> {
                var src = convertValue(u.exp(), list);
                var dst = IRType.Var.makeTemp();
                var op = convertUnaryOp(u);
                list.add(new IRType.Unary(op, src, dst));
                yield dst;
            }
            default -> throw new UnsupportedOperationException(exp.toString());
        };
    }

    private IR.UnaryOperator convertUnaryOp(Ast.Unary u) {
        return switch (u.operator()) {
            case Ast.UnaryOpComplement it -> new IRType.Complement();
            case Ast.UnaryOpNegate it -> new IRType.Negate();
            default -> throw new UnsupportedOperationException(u.toString());
        };
    }

}
