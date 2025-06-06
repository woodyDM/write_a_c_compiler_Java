package cn.deepmax.jfx.asm;

import cn.deepmax.jfx.ir.IR;
import cn.deepmax.jfx.ir.IRType;
import cn.deepmax.jfx.parse.Identifiers;
import cn.deepmax.jfx.parse.TypeChecker;

import java.util.ArrayList;
import java.util.List;

public class AsmAst {

    private IR.Program program;
    private final TypeChecker typeChecker;
    private Asm.PseudoContext pseudoContext;

    final static Asm.Register[] PARAM = new Asm.Register[]{
            Asm.Register.DI,
            Asm.Register.SI,
            Asm.Register.DX,
            Asm.Register.CX,
            Asm.Register.R8D,
            Asm.Register.R9D
    };

    private AsmAst(IR.Program program, TypeChecker typeChecker) {
        this.program = program;
        this.typeChecker = typeChecker;
    }

    public static AssemblyConstruct.Program createAsmAst(IR.Program program, TypeChecker typeChecker) {
        return new AsmAst(program, typeChecker).transform();
    }

    public AssemblyConstruct.Program transform() {
        var pg = (IRType.Program) this.program;
        List<AssemblyConstruct.FunctionDef> list = pg.functionDef().stream()
                .map(this::transFunc)
                .toList();
        return new Asm.AsmProgram(list);
    }

    private AssemblyConstruct.FunctionDef transFunc(IR.FunctionDef functionDef) {
        this.pseudoContext = new Asm.PseudoContext();
        var fn = (IRType.FunctionDef) functionDef;
        List<AssemblyConstruct.Instruction> allIns = new ArrayList<>();
        allIns.add(null); //for AllocateStack
        var params = ((IRType.FunctionDef) functionDef).params();

        //params copy

        var paramSize = params.size();
        for (int i = 0; i < paramSize && i < 6; i++) {
            allIns.add(new Asm.Mov(PARAM[i], pseudoContext.make(params.get(i))));
        }
        for (int i = 6; i < paramSize; i++) {
            //copy on stack
            int offset = 16 + (i - 6) * 8;
            allIns.addAll(Asm.Mov.makeMove(new Asm.Stack(-offset), pseudoContext.make(params.get(i))));
        }
        transInstruction(fn.body(), allIns);
        long varNumber = this.pseudoContext.getPseudoCount();
        allIns.set(0, new Asm.AllocateStack(get16AlignedStack(varNumber)));
        Asm.Function function = new Asm.Function(fn.identifier(), paramSize, 0, varNumber, allIns);
        return function;
    }

    private int get16AlignedStack(long varNumber) {
        if (varNumber % 2 == 0) {
            return (int) varNumber * 4;
        } else {
            return (int) (varNumber + 1) * 4;
        }
    }

    private void transInstruction(List<IR.Instruction> body, List<AssemblyConstruct.Instruction> list) {
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
                case IRType.FunCall call -> {
                    int paramSize = call.args().size();
                    int stackPadding = paramSize % 2 != 0 ? 8 : 0;
                    if (stackPadding != 0) {
                        list.add(new Asm.AllocateStack(8)); //stackPadding
                    }
                    for (int i = 0; i < paramSize && i < 6; i++) {
                        IR.Val val = call.args().get(i);
                        var from = transOperand(val);
                        var to = PARAM[i];
                        list.addAll(Asm.Mov.makeMove(from, to));
                    }
                    for (int i = paramSize - 1; i >= 6; i--) {
                        IR.Val val = call.args().get(i);
                        var from = transOperand(val);
                        if (from instanceof Asm.Register || from instanceof Asm.Imm) {
                            list.add(new Asm.Push(from));
                        } else {
                            list.add(new Asm.Mov(from, Asm.Register.AX));
                            list.add(new Asm.Push(Asm.Register.AX));
                        }
                    }
                    list.add(new Asm.Call(call.functionName()));
                    //adjust stack pointer
                    int bytesToRemove = 8 * (paramSize - 6) * stackPadding;
                    if (bytesToRemove != 0) {
                        list.add(new Asm.DeallocateStack(bytesToRemove));
                    }
                    var result = transOperand(call.dst());
                    list.add(new Asm.Mov(Asm.Register.AX, result));
                }
                default -> throw new UnsupportedOperationException(body.toString());
            }
        }
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
            case IRType.Var v -> this.pseudoContext.make(v.identifier());
            default -> throw new UnsupportedOperationException(exp.toString());
        };
    }
}
