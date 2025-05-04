package cn.deepmax.jfx;

import cn.deepmax.jfx.lexer.Lexer;
import cn.deepmax.jfx.parse.AstNode;
import cn.deepmax.jfx.parse.Parser;

public class App {
//    static String input =
//            """
//                    int main(void){
//                        return 12;    \t\t
//                        \n
//                    }
//                    """;
    static String input =
            """
                    int main(void){  
                        return  10;
                        }
                         
                    """;

    public static void main(String[] args) {
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);
        AstNode root = p.parseProgram();
        System.out.println(root.toString());
    }
}
