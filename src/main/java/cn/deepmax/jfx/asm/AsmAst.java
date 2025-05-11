package cn.deepmax.jfx.asm;

import cn.deepmax.jfx.ir.IR;
import cn.deepmax.jfx.ir.IRType;

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
                    list.addAll(Asm.Mov.makeMove(transOperand(ret.value()), Asm.Register.AX));
                    list.add(new Asm.Ret());
                }
                case IRType.Unary s -> {
                    if (s.op() == IRType.UnaryOp.Not) {
                        list.addAll(Asm.Cmp.make(new Asm.Imm(0), transOperand(s.src())));
                        AssemblyConstruct.Operand dest = transOperand(s.dst());
                        list.addAll(Asm.Mov.makeMove(new Asm.Imm(0), dest));
                        list.add(new Asm.SetCC(Asm.CondiCodeValues.E, dest));
                    } else {
                        list.addAll(Asm.Mov.makeMove(transOperand(s.src()), transOperand(s.dst())));
                        list.add(new Asm.Unary(convertUnaryOp(s.op()), transOperand(s.dst())));
                    }
                }
                case IRType.Binary b -> {
                    IR.BinaryOperator op = b.op();
                    if (op == IRType.BinaryOp.Divide) {
                        list.addAll(Asm.Mov.makeMove(transOperand(b.src1()), Asm.Register.AX));
                        list.add(new Asm.Cdq());
                        list.addAll(Asm.Idiv.make(transOperand(b.src2())));
                        list.addAll(Asm.Mov.makeMove(Asm.Register.AX, transOperand(b.dst())));
                    } else if (op == IRType.BinaryOp.Remainder) {
                        list.addAll(Asm.Mov.makeMove(transOperand(b.src1()), Asm.Register.AX));
                        list.add(new Asm.Cdq());
                        list.addAll(Asm.Idiv.make(transOperand(b.src2())));
                        list.addAll(Asm.Mov.makeMove(Asm.Register.DX, transOperand(b.dst())));
                    } else if (op == IRType.BinaryOp.Add || op == IRType.BinaryOp.Subtract || op == IRType.BinaryOp.Multiply) {
                        list.addAll(Asm.Mov.makeMove(transOperand(b.src1()), transOperand(b.dst())));
                        list.addAll(Asm.Binary.make(
                                        convertBinaryOp(op),
                                        transOperand(b.src2()),
                                        transOperand(b.dst())
                                )
                        );

                    } else if (op instanceof IRType.BinaryOp opp && opp.isLogic()) {
                        list.addAll(Asm.Cmp.make(transOperand(b.src2()), transOperand(b.src1())));
                        AssemblyConstruct.Operand dest = transOperand(b.dst());
                        list.addAll(Asm.Mov.makeMove(new Asm.Imm(0), dest));
                        list.add(new Asm.SetCC(convertCondCode(b.op()), dest));
                    } else {
                        throw new UnsupportedOperationException("invalid op " + b.op());
                    }
                }
                case IRType.Jump jp -> {
                    list.add(new Asm.Jmp(jp.targetIdentifier()));
                }
                case IRType.JumpIfZero jp -> {
                    list.addAll(Asm.Cmp.make(new Asm.Imm(0), transOperand(jp.condition())));
                    list.add(new Asm.JmpCC(Asm.CondiCodeValues.E, jp.target()));
                }
                case IRType.JumpIfNotZero jp -> {
                    list.addAll(Asm.Cmp.make(new Asm.Imm(0), transOperand(jp.condition())));
                    list.add(new Asm.JmpCC(Asm.CondiCodeValues.NE, jp.target()));
                }
                case IRType.Label label -> {
                    list.add(new Asm.Label(label.identifier()));
                }
                case IRType.Copy copy -> {
                    list.addAll(Asm.Mov.makeMove(
                            transOperand(copy.src()),
                            transOperand(copy.dst())
                    ));
                }
                default -> throw new UnsupportedOperationException(body.toString());
            }
        }

        return list;
    }

    private AssemblyConstruct.BinaryOperator convertBinaryOp(IR.BinaryOperator op) {
        return switch (op) {
            case IRType.BinaryOp.Add -> Asm.BinaryOp.Add;
            case IRType.BinaryOp.Subtract -> Asm.BinaryOp.Sub;
            case IRType.BinaryOp.Multiply -> Asm.BinaryOp.Mult;
            default -> throw new UnsupportedOperationException(op.toString());
        };
    }

    private AssemblyConstruct.CondCode convertCondCode(IR.BinaryOperator op) {
        return switch (op) {
            case IRType.BinaryOp.Equal -> Asm.CondiCodeValues.E;
            case IRType.BinaryOp.NotEqual -> Asm.CondiCodeValues.NE;
            case IRType.BinaryOp.LessThan -> Asm.CondiCodeValues.L;
            case IRType.BinaryOp.LessOrEqual -> Asm.CondiCodeValues.LE;
            case IRType.BinaryOp.GreaterThan -> Asm.CondiCodeValues.G;
            case IRType.BinaryOp.GreaterOrEqual -> Asm.CondiCodeValues.GE;
            default -> throw new UnsupportedOperationException(op.toString());
        };
    }

    private AssemblyConstruct.UnaryOperator convertUnaryOp(IR.UnaryOperator op) {
        return switch (op) {
            case IRType.UnaryOp.Complement -> Asm.UnaryOp.Not;
            case IRType.UnaryOp.Negate -> Asm.UnaryOp.Neg;
            default -> throw new UnsupportedOperationException(op.toString());
        };
    }

    private AssemblyConstruct.Operand transOperand(IR.Val exp) {
        return switch (exp) {
            case IRType.Constant c -> new Asm.Imm(c.v());
            case IRType.Var v -> {
                int off = v.getStackOffset();
                //directly return stack
                yield new Asm.Stack(-off);
            }
            default -> throw new UnsupportedOperationException(exp.toString());
        };
    }
}
