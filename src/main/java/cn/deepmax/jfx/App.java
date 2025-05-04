package cn.deepmax.jfx;

import cn.deepmax.jfx.lexer.Lexer;
import cn.deepmax.jfx.parse.AST;
import cn.deepmax.jfx.parse.Parser;

public class App {
        static String input =
            """
                    int main   \t\t\r  \n
                    (  void ) {
                        return 12;    \t\t
                        \n
                    }af
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
        AST.Program root = p.parseProgram();
        System.out.println(root.toString());
    }
}
