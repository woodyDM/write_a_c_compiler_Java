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
    NEG("-", 45),
    DECREMENT("--"),

    PLUS("+", 45),
    MULTIP("*", 50),
    DIV("/", 50),
    REMINDER("%", 50),
    EOF,
    ;


    public final String value;
    public final Integer prec;

    TokenType() {
        this(null, null);
    }

    TokenType(String value) {
        this(value, null);
    }

    TokenType(String value, Integer p) {
        this.value = value;
        this.prec = p;
    }

    public int prec() {
        if ((prec == null)) {
            throw new UnsupportedOperationException("invalid op " + this.name());
        }
        return prec.intValue();
    }

    public boolean isBinaryOp() {
        return this == PLUS || this == MULTIP || this == DIV || this == REMINDER || this == NEG;
    }

    @Override
    public String toString() {
        return value == null || value.isBlank() ? this.name() : "[" + value + "]";
    }
}
