package cn.deepmax.jfx;

import cn.deepmax.jfx.asm.AsmAst;
import cn.deepmax.jfx.asm.AssemblyConstruct;
import cn.deepmax.jfx.emit.Emission;
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
                        return -4;    \t\t
                        \n
                    }
                    """;
//    static String input =
//            """
//                    int main(void){
//                        return  10;
//                        }
//                    """;


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
            runTest(inputSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runTest(String source) {
        Lexer lexer = new Lexer(source);
        Parser p = new Parser(lexer);
        Ast.AstProgram ast = p.parseProgram();
        AssemblyConstruct.Program asmAst = AsmAst.createAsmAst(ast);
        String asmCode = Emission.codegen(asmAst);

    }

    public static void mainLocal(String[] args) {
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);
        System.out.println("--------- lexer ----------");
        System.out.println(p.tokenList);

        Ast.AstProgram ast = p.parseProgram();
        System.out.println("--------- parser ---------");
        System.out.println(ast.toString());

        AssemblyConstruct.Program asmAst = AsmAst.createAsmAst(ast);
        System.out.println("--------- asm ast ---------");
        System.out.println(asmAst);

        String asmCode = Emission.codegen(asmAst);
        System.out.println("---- asm -------");
        System.out.println(asmCode);
    }
}
