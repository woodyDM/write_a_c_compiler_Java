package cn.deepmax.jfx;

import cn.deepmax.jfx.asm.AsmAst;
import cn.deepmax.jfx.asm.AssemblyConstruct;
import cn.deepmax.jfx.emit.Emission;
import cn.deepmax.jfx.ir.IR;
import cn.deepmax.jfx.ir.IRConverter;
import cn.deepmax.jfx.lexer.Lexer;
import cn.deepmax.jfx.parse.Ast;
import cn.deepmax.jfx.parse.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    static String input =
            """
                    int main   \t\t\r  \n
                    (  void ) {
                        return ~(-4);    \t\t
                        \n
                    }
                    """;
//    static String input =
//            """
//                    int main(void){
//                        return  10;
//                        }
//                    """;

    enum TestLevel {
        LEX,
        PARSE,
        CODEGEN
    }

    public static void main(String[] args) {
        String v = System.getProperty("test");
        if (v == null || v.isBlank()) {
            mainLocal(args);
            return;
        }
        //for book's tests
        String path = args[1];
        try {
            String inputSource = Files.readString(Paths.get(path));
            runTest(args[0], inputSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runTest(String level, String source) {
        Lexer lexer = new Lexer(source);
        Parser p = new Parser(lexer);
        if ("--lex".equals(level)) return;

        Ast.AstProgram astProgram = p.parseProgram();
        if ("--parse".equals(level)) return;

        IRConverter irConverter = new IRConverter(astProgram);
        IR.Program irProgram = irConverter.convertToIR();
        if ("--tacky".equals(level)) return;


        AssemblyConstruct.Program asmAst = AsmAst.createAsmAst(irProgram);
        if ("--codegen".equals(level)) return;
        //String asmCode = Emission.codegen(asmAst);

    }

    public static void mainLocal(String[] args) {
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);
        System.out.println("--------- lexer ----------");
        System.out.println(p.tokenList);

        Ast.AstProgram ast = p.parseProgram();
        System.out.println("--------- parser ---------");
        System.out.println(ast.toString());


        IRConverter irConverter = new IRConverter(ast);
        IR.Program irProgram = irConverter.convertToIR();
        System.out.println("--------- ir ---------");
        System.out.println(irProgram.toString());

        AssemblyConstruct.Program asmAst = AsmAst.createAsmAst(irProgram);
        System.out.println("--------- asm ast ---------");
        System.out.println(asmAst);

        String asmCode = Emission.codegen(asmAst);
        System.out.println("---- asm -------");
        System.out.println(asmCode);
    }
}
