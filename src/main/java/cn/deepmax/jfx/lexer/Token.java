package cn.deepmax.jfx.lexer;

public interface Token {

    TokenType type();

    TokenParams params();

    default boolean isKeyword(String word) {
        return type() == TokenType.KEYWORD && params().toString().equals(word);
    }

}
