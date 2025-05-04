package cn.deepmax.jfx;

import cn.deepmax.jfx.asm.AsmAst;
import cn.deepmax.jfx.asm.AssemblyConstruct;
import cn.deepmax.jfx.lexer.Lexer;
import cn.deepmax.jfx.parse.Ast;
import cn.deepmax.jfx.parse.Parser;

public class App {
    static String input =
            """
                    int main   \t\t\r  \n
                    (  void ) {
                        return 12;    \t\t
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
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);
        Ast.AstProgram ast = p.parseProgram();
        AssemblyConstruct.Program asmAst = AsmAst.createAsmAst(ast);

        System.out.println(ast.toString());
        System.out.println(asmAst);
    }
}
