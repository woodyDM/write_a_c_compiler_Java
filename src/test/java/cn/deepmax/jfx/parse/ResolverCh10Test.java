package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.exception.SemanticException;
import cn.deepmax.jfx.lexer.Lexer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResolverCh10Test {

    @Test
    void should_ok_when_static_func_in_file_scope() {
        String input = """
                    static int some_func();
                    int main(void) {
                        return 0;
                    }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        assertDoesNotThrow(() -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolver.resolveProgram(ast);
            new TypeChecker().checkProgram(ast);

        }, "should not throw");
    }

    @Test
    void should_error_when_block_func_declare_contains_static() {
        String input = """
                    int main(void) {
                        static int some_func();
                        return 0;
                    }
                """;
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);

        SemanticException ex = assertThrows(SemanticException.class, () -> {
            Ast.AstProgram ast = p.parseProgram();
            ast = p.resolver.resolveProgram(ast);
            new TypeChecker().checkProgram(ast);

        }, "should throw");
        assertTrue(ex.getMessage().startsWith("block function declaration contains static"), ex.getMessage());
    }

}