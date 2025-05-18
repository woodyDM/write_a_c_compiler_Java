package cn.deepmax.jfx.lexer;

public class LexerException extends RuntimeException {

    public LexerException(Lexer lexer, String msg) {
        super(String.format("lexer error: [%s] at pos %d, near [%s]", msg, lexer.pos, lexer.nearSubString()));
    }

}
