package cn.deepmax.jfx.asm;

import java.util.List;

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

    public record Ret() implements AssemblyConstruct.Instruction {

    }

    public record Mov(AssemblyConstruct.Operand src,
                      AssemblyConstruct.Operand dest) implements AssemblyConstruct.Instruction {
        public static List<Mov> makeMove(AssemblyConstruct.Operand src,
                                         AssemblyConstruct.Operand dest) {
            if (src instanceof Stack && dest instanceof Stack) {
                return List.of(
                        new Mov(src, Register.R10D()),
                        new Mov(Register.R10D(), dest)
                );
            } else {
                return List.of(new Mov(src, dest));
            }
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
