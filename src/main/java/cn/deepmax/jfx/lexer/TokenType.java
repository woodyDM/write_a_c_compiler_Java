package cn.deepmax.jfx.lexer;

public enum TokenType {

    ID,
    CONSTANT,
    KEYWORD,
    OPEN_PARENTHESIS("("),
    CLOSE_PARENTHESIS(")"),
    OPEN_BRACE("{"),
    CLOSE_BRACE("}"),
    SEMICOLON(";"),

    BITWISE("~"),
    NEG("-"),
    DECREMENT("--"),

    PLUS("+"),
    MULTIP("*"),
    DIV("/"),
    REMINDER("%"),
    EOF,
    ;


    public final String value;

    TokenType() {
        this(null);
    }

    TokenType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value == null || value.isBlank() ? this.name() : "[" + value + "]";
    }
}
