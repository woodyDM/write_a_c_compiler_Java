package cn.deepmax.jfx;

import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class App {
    static String input =
            """
                    int main(void){  
                        return 2;    \t\t
                        \n
                    }  
                    """;

    public static void main(String[] args) {
        Lexer lexer = new Lexer(input);
        Token t=null;
        while ((t = lexer.nextToken()) != null) {
            System.out.println(t);
        }
    }
}
