package cn.deepmax.jfx.emit;

import cn.deepmax.jfx.asm.Asm;
import cn.deepmax.jfx.asm.AsmAst;
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
        genProgram();
        genFuncdef();
        genInstruction();
        return sb.toString();
    }

    private void genInstruction() {
        AssemblyConstruct.FunctionDef functionDef = program.functionDef;
        Asm.Function fn = (Asm.Function) functionDef;
        List<AssemblyConstruct.Instruction> instructions = fn.instructions;
        for (AssemblyConstruct.Instruction it : instructions) {
            sb.append("\t");
            switch (it) {
                case Asm.Mov mov -> sb.append("movl ").append(genOperand(mov.src)).append(", ")
                        .append(genOperand(mov.dest)) ;

                case Asm.Ret ret -> sb.append("ret");
                default -> throw new UnsupportedOperationException(it.toString());
            }
            sb.append("\n");
        }
    }

    private String genOperand(AssemblyConstruct.Operand op) {
        return switch (op) {
            case Asm.Imm imm -> String.format("$%d", imm.v);
            case Asm.Register register -> "%eax";
            default -> throw new UnsupportedOperationException(op.toString());
        };
    }

    private void genFuncdef() {
        AssemblyConstruct.FunctionDef functionDef = program.functionDef;
        Asm.Function fn = (Asm.Function) functionDef;
        sb.append("\t.globl ").append(fn.name).append("\n")
                .append(fn.name).append(":\n");
    }

    private void genProgram() {
        sb.append("#Program:\n").append("\t.section .not.GNU-stack,\"\",@progbits\n");
    }
}
