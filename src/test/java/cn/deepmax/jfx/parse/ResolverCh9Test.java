package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.exception.SemanticException;
import cn.deepmax.jfx.lexer.Lexer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResolverCh9Test {

    @Test
    void test_call_no_func_def() {
        String input = """
                    int main(void) {
                        int x = fib(4);  
                        return x / (y % 256);
                    }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);


        SemanticException ex = assertThrows(SemanticException.class, () -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        }, "should throw");
        assertTrue(ex.getMessage().startsWith("Can't call on undeclared function"), ex.getMessage());
    }

    @Test
    void test_call_on_var() {
        String input = """
                
                    int main(void) {
                        int fb=5;
                        int x = fb(4);  
                        return x;
                    }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);


        SemanticException ex = assertThrows(SemanticException.class, () -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        }, "should throw");
        assertTrue(ex.getMessage().startsWith("Can't do function call on var"), ex.getMessage());
    }

    @Test
    void test_call_param_mismatch() {
        String input = """
                    int fb(int a,int b);
                    int main(void) {
                        int x = fb(4);  
                        return x;
                    }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);


        SemanticException ex = assertThrows(SemanticException.class, () -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        }, "should throw");
        assertTrue(ex.getMessage().startsWith("function call need 2 args, but only provide 1"), ex.getMessage());
    }

    @Test
    void should_ok_fun_in_body() {
        String input = """
                    int main(void) {
                        int fb(int a,int b);     
                        int x = fb(4,5);  
                        return x;
                    }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);


        assertDoesNotThrow(() -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        });
    }

    @Test
    void should_ok_fun_global() {
        String input = """
                    int fb(int a,int b);
                    int main(void) {
                        int x = fb(4,5);  
                        return x;
                    }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        assertDoesNotThrow(() -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        });
    }
    @Test
    void should_ok_complex_program() {
        String input = """
                           
                    int fib(int a);
                    
                    int multiply_many_args(int a, int b, int c, int d, int e, int f, int g, int h){
                        return a+b;
                    }
                    
                    int main(void) {
                        int x = fib(4);  
                    
                        int seven = 7;
                        int eight = fib(6);
                        int y = multiply_many_args(x, 2, 3, 4, 5, 6, seven,x);
                        if (x != 3) {
                            return 1;
                        }
                        if (y != 589680/2) {
                            return 2;
                        }
                        return x / (y % 256);
                    }
                    
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        assertDoesNotThrow(() -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        });
    }

    @Test
    void should_ok_fun_global_and_var() {
        String input = """
                     int b();
                  int main(void){
                
                
                      int b=5;
                      return b;
                  }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        assertDoesNotThrow(() -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);

        });
    }

    @Test
    void should_error_when_duplicate_define_var() {
        String input = """
                  int main(void){
                      int b;
                      int b=5;  
                      return b;
                  }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        }, "should throw");
        assertTrue(ex.getMessage().startsWith("Duplicate variable declaration!"), ex.getMessage());
    }


    @Test
    void should_not_throw_error_when_duplicate_define_in_other_scope() {
        String input = """
                  int main(void){
                      int b=0;
                      for(int b=5;b<10;b=b+1) {
                        b=b+2;
                      }
                      return b;
                  }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        assertDoesNotThrow(() -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        }, "should not throw");

    }

    @Test
    void should_error_when_fun_and_var_same_scope() {
        String input = """
                  int main(void){
                      int b();
                      int b=5;
                      return b;
                  }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        }, "should throw");
        assertTrue(ex.getMessage().startsWith("Id redeclared as different kind of symbol!"), ex.getMessage());
    }

    @Test
    void should_error_when_fun_param_duplicate() {
        String input = """
                  int main(void){
                
                      int b(int a,int a);
                
                      return b;
                  }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        }, "should throw");
        assertTrue(ex.getMessage().startsWith("Duplicate variable declaration!"), ex.getMessage());
    }

    @Test
    void should_error_when_fun_param_duplicate2() {
        String input = """
                  int bb(int a){
                    int a=5;
                    return a;
                  }
                  int main(void){
                      return 0;
                  }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        }, "should throw");
        assertTrue(ex.getMessage().startsWith("Duplicate variable declaration!"), ex.getMessage());
    }

    @Test
    void should_error_when_fun_def_not_same() {
        String input = """
                  int bb(int a,int b);
                  int main(void){
                      int bb(int a);
                      return 0;
                  }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        }, "should throw");
        assertTrue(ex.getMessage().startsWith("declaration is incompatible with previous"), ex.getMessage());
    }

    @Test
    void should_no_error_when_fun_def_same_multi_times() {
        String input = """
                  int bb(int a,int b);
                  int main(void){
                      int bb(int a,int b);
                      return 0;
                  }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        assertDoesNotThrow(() -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        });
    }

    @Test
    void should_no_error_when_fun_def_same_multi_times_2() {
        String input = """
                  
                  int main(void){
                      int bb(int a,int b);
                      int bb(int a,int b);
                      return 0;
                  }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        assertDoesNotThrow(() -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolveProgram(ast);
        });
    }

}