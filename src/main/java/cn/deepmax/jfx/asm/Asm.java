package cn.deepmax.jfx.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Asm {

    public record AsmProgram(AssemblyConstruct.FunctionDef functionDef) implements AssemblyConstruct.Program {

    }

    public record Function(String name,
                           List<AssemblyConstruct.Instruction> instructions)
            implements AssemblyConstruct.FunctionDef {

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
            if (op instanceof AddOp || op instanceof SubOp) {
                if (operand instanceof Stack && dst instanceof Stack) {
                    result.addAll(Mov.makeMove(operand, new Register(new R10D())));
                    result.add(new Binary(op, new Register(new R10D()), dst));
                } else {
                    result.add(new Binary(op, operand, dst));
                }
            } else if (op instanceof MultOp) {
                if (dst instanceof Stack) {
                    Register tempRegister = new Register(new R11D());
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
                                new Register(new R10D())
                        ),
                        new Idiv(new Register(new R10D()))
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

    public static <T> List<T> fixBothStack(AssemblyConstruct.Operand src,
                                           AssemblyConstruct.Operand dest,
                                           BiFunction<AssemblyConstruct.Operand, AssemblyConstruct.Operand, T> construct) {
        if (src instanceof Stack && dest instanceof Stack) {
            return List.of(
                    construct.apply(src, Register.R10D()),
                    construct.apply(Register.R10D(), dest)
            );
        } else {
            return List.of(construct.apply(src, dest));
        }

    }

    public record AllocateStack(int size) implements AssemblyConstruct.Instruction {

    }

    public record Neg() implements AssemblyConstruct.UnaryOperator {
    }

    public record Not() implements AssemblyConstruct.UnaryOperator {
    }

    public record AX() implements AssemblyConstruct.Reg {
    }

    public record R10D() implements AssemblyConstruct.Reg {
    }

    public record DX() implements AssemblyConstruct.Reg {
    }

    public record R11D() implements AssemblyConstruct.Reg {
    }

    public record AddOp() implements AssemblyConstruct.BinaryOperator {

    }

    public record SubOp() implements AssemblyConstruct.BinaryOperator {
    }

    public record MultOp() implements AssemblyConstruct.BinaryOperator {
    }

    public record Stack(int pos) implements AssemblyConstruct.Operand {
    }

    public record Pseudo(String id) implements AssemblyConstruct.Operand {

    }

    public record Imm(int v) implements AssemblyConstruct.Operand {

    }

    public record Register(AssemblyConstruct.Reg reg) implements AssemblyConstruct.Operand {

        public static Register R10D() {
            return new Register(new R10D());
        }
    }


}
