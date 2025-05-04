package cn.deepmax.jfx.lexer;

public enum TokenType {
    ID,
    CONSTANT,
    KEYWORD,
    OPEN_PARENTHESIS, //(
    CLOSE_PARENTHESIS, //)
    OPEN_BRACE,//{
    CLOSE_BRACE,//}
    SEMICOLON, //;

    EOF,
}
