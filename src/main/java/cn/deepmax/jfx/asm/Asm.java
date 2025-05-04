package cn.deepmax.jfx.asm;

import java.util.List;
import java.util.stream.Collectors;

public class Asm {

    public static class AsmProgram implements AssemblyConstruct.Program {
        public AssemblyConstruct.FunctionDef functionDef;

        @Override
        public String toString() {
            return String.format("AsmProgram(%s)", functionDef.toString());
        }
    }

    public static class Function implements AssemblyConstruct.FunctionDef {
        public final String name;
        public List<AssemblyConstruct.Instruction> instructions;

        public Function(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("Function(name=%s;instructions=%s)", name, instructions
                    .stream().map(m -> m.toString()).collect(Collectors.joining(",")));
        }
    }

    public static class Ret implements AssemblyConstruct.Instruction {
        @Override
        public String toString() {
            return "Ret";
        }
    }

    public static class Mov implements AssemblyConstruct.Instruction {
        public final AssemblyConstruct.Operand src;
        public final AssemblyConstruct.Operand dest;

        public Mov(AssemblyConstruct.Operand src, AssemblyConstruct.Operand dest) {
            this.src = src;
            this.dest = dest;
        }

        @Override
        public String toString() {
            return String.format("Mov(src=%s,dest=%s)", src, dest);
        }
    }

    public static class Imm implements AssemblyConstruct.Operand {
        public final int v;

        public Imm(int v) {
            this.v = v;
        }

        @Override
        public String toString() {
            return String.format("Imm(%s)", v + "");
        }
    }

    public static class Register implements AssemblyConstruct.Operand {
        @Override
        public String toString() {
            return "Register";
        }
    }


}
