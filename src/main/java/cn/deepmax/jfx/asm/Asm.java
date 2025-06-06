package cn.deepmax.jfx.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

public class Asm {
    private Asm() {
    }

    public record AsmProgram(List<AssemblyConstruct.FunctionDef> functionDef) implements AssemblyConstruct.Program {

    }

    public record Function(String name,
                           int paramSize,
                           long tempVarBase,
                           long tempVarSize,
                           List<AssemblyConstruct.Instruction> instructions) implements AssemblyConstruct.FunctionDef {
    }

    public record Unary(AssemblyConstruct.UnaryOperator op,
                        AssemblyConstruct.Operand operand) implements AssemblyConstruct.Instruction {

    }

    public record Binary(AssemblyConstruct.BinaryOperator op,
                         AssemblyConstruct.Operand operand,
                         AssemblyConstruct.Operand dst) implements AssemblyConstruct.Instruction {
        public static List<AssemblyConstruct.Instruction> make(AssemblyConstruct.BinaryOperator op,
                                                               AssemblyConstruct.Operand operand,
                                                               AssemblyConstruct.Operand dst) {
            List<AssemblyConstruct.Instruction> result = new ArrayList<>();
            if (op == BinaryOp.Add || op == BinaryOp.Sub) {
                if (operand instanceof Pseudo && dst instanceof Pseudo) {
                    result.addAll(Mov.makeMove(operand, Register.R10D));
                    result.add(new Binary(op, Register.R10D, dst));
                } else {
                    result.add(new Binary(op, operand, dst));
                }
            } else if (op == BinaryOp.Mult) {
                if (dst instanceof Pseudo) {
                    Register tempRegister = Register.R11D;
                    result.addAll(Mov.makeMove(dst, tempRegister));
                    result.add(new Binary(op, operand, tempRegister));
                    result.addAll(Mov.makeMove(tempRegister, dst));
                } else {
                    result.add(new Binary(op, operand, dst));
                }
            } else {
                throw new UnsupportedOperationException(op.toString());
            }
            return result;
        }

    }

    public record Idiv(AssemblyConstruct.Operand operand) implements AssemblyConstruct.Instruction {
        public static List<AssemblyConstruct.Instruction> make(AssemblyConstruct.Operand operand) {
            if (operand instanceof Imm) {
                return List.of(
                        new Mov(
                                operand,
                                Register.R10D
                        ),
                        new Idiv(Register.R10D)
                );
            } else {
                return List.of(new Idiv(operand));
            }
        }
    }

    public record Cdq() implements AssemblyConstruct.Instruction {
    }


    public record Ret() implements AssemblyConstruct.Instruction {
    }

    public record Mov(AssemblyConstruct.Operand src,
                      AssemblyConstruct.Operand dest) implements AssemblyConstruct.Instruction {
        public static List<Mov> makeMove(AssemblyConstruct.Operand src,
                                         AssemblyConstruct.Operand dest) {
            return fixBothStack(src, dest, Mov::new);
        }
    }

    public record Cmp(AssemblyConstruct.Operand left,
                      AssemblyConstruct.Operand right) implements AssemblyConstruct.Instruction {
        public static List<AssemblyConstruct.Instruction> make(AssemblyConstruct.Operand left,
                                                               AssemblyConstruct.Operand right) {
            if (left instanceof Pseudo && right instanceof Pseudo) {
                return List.of(
                        new Mov(left, Register.R10D),
                        new Cmp(Register.R10D, right)
                );
            } else if (right instanceof Imm) {
                return List.of(
                        new Mov(right, Register.R11D),
                        new Cmp(left, Register.R11D)
                );
            } else {
                return List.of(new Cmp(left, right));
            }
        }
    }

    public record Jmp(String targetId) implements AssemblyConstruct.Instruction {
    }

    public record Label(String id) implements AssemblyConstruct.Instruction {
    }

    public record JmpCC(AssemblyConstruct.CondCode condition,
                        String targetId) implements AssemblyConstruct.Instruction {
    }

    public record SetCC(AssemblyConstruct.CondCode condition,
                        AssemblyConstruct.Operand operand) implements AssemblyConstruct.Instruction {
    }

    public static <T> List<T> fixBothStack(AssemblyConstruct.Operand src,
                                           AssemblyConstruct.Operand dest,
                                           BiFunction<AssemblyConstruct.Operand, AssemblyConstruct.Operand, T> construct) {
        if (src instanceof Pseudo && dest instanceof Pseudo) {
            return List.of(
                    construct.apply(src, Register.R10D),
                    construct.apply(Register.R10D, dest)
            );
        } else {
            return List.of(construct.apply(src, dest));
        }

    }

    public record AllocateStack(long size) implements AssemblyConstruct.Instruction {
    }

    public record DeallocateStack(long size) implements AssemblyConstruct.Instruction {

    }

    public record Call(String identifier) implements AssemblyConstruct.Instruction {

    }

    public record Push(AssemblyConstruct.Operand operand) implements AssemblyConstruct.Instruction {

    }


    public enum UnaryOp implements AssemblyConstruct.UnaryOperator {
        Neg,
        Not
    }

    public enum Registers implements AssemblyConstruct.Reg {
        AX,
        CX,
        DX,
        DI,
        SI,
        R8D,
        R9D,
        R10D,
        R11D
    }

    public enum BinaryOp implements AssemblyConstruct.BinaryOperator {
        Add,
        Sub,
        Mult,
    }

    public enum CondiCodeValues implements AssemblyConstruct.CondCode {
        E,
        NE,
        G,
        GE,
        L,
        LE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public record Stack(int pos) implements AssemblyConstruct.Operand {
    }

    public record Pseudo(String id) implements AssemblyConstruct.Operand {
        public static final AtomicLong seq = new AtomicLong(0);

        public static Pseudo make(String id) {
            seq.getAndIncrement();
            return new Pseudo(id);
        }

    }

    public record Imm(int v) implements AssemblyConstruct.Operand {
    }

    public record Register(AssemblyConstruct.Reg reg) implements AssemblyConstruct.Operand {
        public static final Register R10D = new Register(Registers.R10D);
        public static final Register R11D = new Register(Registers.R11D);
        public static final Register AX = new Register(Registers.AX);
        public static final Register DX = new Register(Registers.DX);

    }


}
