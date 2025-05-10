package cn.deepmax.jfx.asm;

import cn.deepmax.jfx.ir.IR;
import cn.deepmax.jfx.ir.IRType;
import cn.deepmax.jfx.parse.Ast;
import cn.deepmax.jfx.parse.AstNode;

import java.util.ArrayList;
import java.util.List;

public class AsmAst {

    private IR.Program program;

    private AsmAst(IR.Program program) {
        this.program = program;
    }


    public static AssemblyConstruct.Program createAsmAst(IR.Program program) {
        return new AsmAst(program).transform();
    }

    public AssemblyConstruct.Program transform() {
        var pg = (IRType.Program) this.program;
        var fn = transFunc(pg.functionDef());
        Asm.AsmProgram p = new Asm.AsmProgram(fn);
        return p;
    }

    private AssemblyConstruct.FunctionDef transFunc(IR.FunctionDef functionDef) {
        var fn = (IRType.FunctionDef) functionDef;
        var ins = transInstruction(fn.body());
        Asm.Function function = new Asm.Function(fn.identifier(), ins);
        return function;
    }

    private List<AssemblyConstruct.Instruction> transInstruction(List<IR.Instruction> body) {
        List<AssemblyConstruct.Instruction> list = new ArrayList<>();
        //first AllocateStaci
        list.add(new Asm.AllocateStack(IRType.Var.varId.get() * 4));
        for (IR.Instruction ir : body) {
            switch (ir) {
                case IRType.Return ret -> {
                    list.addAll(Asm.Mov.makeMove(transOperand(ret.value()), new Asm.Register(new Asm.AX())));
                    list.add(new Asm.Ret());
                }
                case IRType.Unary s -> {
                    list.addAll(Asm.Mov.makeMove(transOperand(s.src()), transOperand(s.dst())));
                    list.add(new Asm.Unary(convertUnaryOp(s.op()), transOperand(s.dst())));
                }
                default -> throw new UnsupportedOperationException(body.toString());
            }
        }

        return list;
    }

    private AssemblyConstruct.UnaryOperator convertUnaryOp(IR.UnaryOperator op) {
        return switch (op) {
            case IRType.Complement _i -> new Asm.Not();
            case IRType.Negate _i -> new Asm.Neg();
            default -> throw new UnsupportedOperationException(op.toString());
        };
    }

    private AssemblyConstruct.Operand transOperand(IR.Val exp) {
        return switch (exp) {
            case IRType.Constant c -> new Asm.Imm(c.v());
            case IRType.Var v -> {
                int off = v.getStackOffset();
                yield new Asm.Stack(-off);
            }
            default -> throw new UnsupportedOperationException(exp.toString());
        };
    }
}
