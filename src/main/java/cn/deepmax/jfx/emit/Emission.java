package cn.deepmax.jfx.emit;

import cn.deepmax.jfx.asm.Asm;
import cn.deepmax.jfx.asm.AssemblyConstruct;

import java.util.List;

public class Emission {

    private final Asm.AsmProgram program;
    private StringBuilder sb = new StringBuilder();

    private Emission(Asm.AsmProgram program) {
        this.program = program;
    }

    public static String codegen(AssemblyConstruct.Program program) {
        Asm.AsmProgram pro = (Asm.AsmProgram) program;
        Emission emission = new Emission(pro);
        return emission.codegen();
    }

    public String codegen() {
        for (AssemblyConstruct.FunctionDef functionDef : program.functionDef()) {
            genFuncdefGlobl(functionDef);
        }
        for (AssemblyConstruct.FunctionDef functionDef : program.functionDef()) {
            genFuncPrepareStackRegister(functionDef);
            genInstruction(functionDef);
        }

        genProgram();
        return sb.toString();
    }

    private void genFuncPrepareStackRegister(AssemblyConstruct.FunctionDef functionDef) {
        Asm.Function fn = (Asm.Function) functionDef;
        sb.append(fn.name()).append(":\n");
        pushIns("pushq %rbp");
        pushIns("movq  %rsp, %rbp");
    }

    private void genFuncdefGlobl(AssemblyConstruct.FunctionDef functionDef) {
        Asm.Function fn = (Asm.Function) functionDef;
        sb.append("\t.globl ").append(fn.name()).append("\n");
    }

    private void genInstruction(AssemblyConstruct.FunctionDef functionDef) {
        Asm.Function fn = (Asm.Function) functionDef;
        List<AssemblyConstruct.Instruction> instructions = fn.instructions();
        for (AssemblyConstruct.Instruction it : instructions) {

            switch (it) {
                case Asm.Mov mov -> sb.append("\tmovl\t")
                        .append(genOperand(mov.src())).append(",\t")
                        .append(genOperand(mov.dest())).append("\n");

                case Asm.Ret ret -> {
                    pushIns("movq\t%rbp,\t%rsp");
                    pushIns("popq\t%rbp");
                    pushIns("ret");
                }
                case Asm.Unary un -> {
                    String uop = genUnaryOperator(un.op());
                    String opd = genOperand(un.operand());
                    pushIns(String.format("%s\t%s", uop, opd));
                }
                case Asm.Binary b -> {
                    String op = genBinaryOperator(b.op());
                    String src = genOperand(b.operand());
                    String dst = genOperand(b.dst());
                    pushIns(String.format("%s\t%s,\t%s", op, src, dst));
                }
                case Asm.Cdq c -> pushIns("cdq");
                case Asm.Idiv d -> pushIns("idivl\t" + genOperand(d.operand()));
                case Asm.AllocateStack s -> pushIns("subq\t$" + s.size() + ",\t%rsp");
                case Asm.DeallocateStack s -> pushIns("addq\t$" + s.size() + ",\t%rsp");
                case Asm.Cmp cmp -> {
                    String left = genOperand(cmp.left());
                    String right = genOperand(cmp.right());
                    pushIns(String.format("cmpl\t%s,\t%s", left, right));
                }
                case Asm.Jmp jp -> pushIns(String.format("jmp\t.L%s", jp.targetId()));
                case Asm.JmpCC jpc -> pushIns(String.format("j%s\t.L%s", jpc.condition().toString(), jpc.targetId()));
                case Asm.SetCC scc ->
                        pushIns(String.format("set%s\t%s", scc.condition().toString(), genOperand1Byte(scc.operand())));
                case Asm.Label label -> {
                    String ins = String.format(".L%s :", label.id());
                    sb.append((ins)).append("\n");
                }
                case Asm.Push p -> {
                    pushIns(String.format("pushq\t%s", genOperand(p.operand(), 8)));
                }
                case Asm.Call c -> {
                    pushIns(String.format("call\t%s@PLT", c.identifier()));
                }
                default -> throw new UnsupportedOperationException(it.toString());
            }

        }
    }

    private String genUnaryOperator(AssemblyConstruct.UnaryOperator op) {
        return switch (op) {
            case Asm.UnaryOp.Neg -> "negl";
            case Asm.UnaryOp.Not -> "notl";
            default -> throw new UnsupportedOperationException(op.toString());
        };
    }

    private String genBinaryOperator(AssemblyConstruct.BinaryOperator op) {
        return switch (op) {
            case Asm.BinaryOp.Add -> "addl";
            case Asm.BinaryOp.Sub -> "subl";
            case Asm.BinaryOp.Mult -> "imull";
            default -> throw new UnsupportedOperationException(op.toString());
        };
    }

    private String genOperand1Byte(AssemblyConstruct.Operand op) {
        return genOperand(op, 1);
    }

    private String genOperand(AssemblyConstruct.Operand op) {
        return genOperand(op, 4);
    }

    private String genOperand(AssemblyConstruct.Operand op, int byteSize) {
        return switch (op) {
            case Asm.Register register -> {
                Asm.Registers reg = (Asm.Registers) register.reg();
                yield switch (byteSize) {
                    case 1 -> "%" + reg.b1;
                    case 4 -> "%" + reg.b4;
                    case 8 -> "%" + reg.b8;
                    default -> throw new IllegalStateException("invalid " + byteSize);
                };
            }
            case Asm.Stack s -> s.pos() + "(%rbp)";
            case Asm.Pseudo p -> {
                //allocate as stack
                int offset = -p.getOffset();
                yield offset + "(%rbp)";
            }
            case Asm.Imm imm -> "$" + imm.v();
            default -> throw new UnsupportedOperationException(op.toString());
        };
    }


    private void pushIns(String ins) {
        sb.append("\t").append((ins)).append("\n");
    }

    private void genProgram() {
        sb.append("#Program:\n").append("\t.section .not.GNU-stack,\"\",@progbits\n");
    }
}
