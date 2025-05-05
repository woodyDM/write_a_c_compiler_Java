package cn.deepmax.jfx.asm;

import cn.deepmax.jfx.parse.Ast;
import cn.deepmax.jfx.parse.AstNode;

import java.util.ArrayList;
import java.util.List;

public class AsmAst {

    private Ast.AstProgram program;

    private AsmAst(Ast.AstProgram program) {
        this.program = program;
    }

    public static AssemblyConstruct.Program createAsmAst(Ast.AstProgram program) {
        return new AsmAst(program).transform();
    }

    public AssemblyConstruct.Program transform() {
        Asm.AsmProgram p = new Asm.AsmProgram();
        p.functionDef = transFunc(program.functionDefinition());
        return p;
    }

    private AssemblyConstruct.FunctionDef transFunc(Ast.FunctionDefinition functionDef) {
        Asm.Function function = new Asm.Function(functionDef.name());
        function.instructions = transInstruction(functionDef.body());
        return function;
    }

    private List<AssemblyConstruct.Instruction> transInstruction(AstNode.Statement body) {
        List<AssemblyConstruct.Instruction> list = new ArrayList<>();
        switch (body) {
            case Ast.ReturnStatement s -> {
                list.add(new Asm.Mov(transOperand(s.exp()), new Asm.Register()));
                list.add(new Asm.Ret());
            }
            default -> throw new UnsupportedOperationException(body.toString());
        }
        return list;
    }

    private AssemblyConstruct.Operand transOperand(AstNode.Exp exp) {
        switch (exp) {
            case Ast.IntExp ip -> {
                return new Asm.Imm(ip.value());
            }
            default -> throw new UnsupportedOperationException(exp.toString());
        }
    }
}
