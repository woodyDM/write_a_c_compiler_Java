package cn.deepmax.jfx.lexer;

public enum TokenType implements Token {

    ID,
    CONSTANT,
    KEYWORD,
    OPEN_PARENTHESIS("("),
    CLOSE_PARENTHESIS(")"),
    OPEN_BRACE("{"),
    CLOSE_BRACE("}"),
    SEMICOLON(";"),

    BITWISE("~"),
    NEG("-", 45), //MINUS
    DECREMENT("--"),

    PLUS("+", 45),
    MULTIP("*", 50),
    DIV("/", 50),
    REMINDER("%", 50),

    //logic op
    NOT("!"),
    AND("&&", 10),
    OR("||", 5),
    EQUAL_TO("==", 30),
    NOT_EQUAL_TO("!=", 30),
    LESS_THAN("<", 35),
    GREATER_THAN(">", 35),
    LESS_THAN_OR_EQ("<=", 35),
    GREATER_THAN_OR_EQ(">=", 35),

    ASSIGNMENT("=", 1),

    QUESTION("?", 3),
    COLON(":"),

    COMMA(","),
    SHARP("#"),


    NEWLINE,
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
        return this == PLUS || this == MULTIP || this == DIV || this == REMINDER || this == NEG ||
                this == LESS_THAN || this == LESS_THAN_OR_EQ || this == GREATER_THAN || this == GREATER_THAN_OR_EQ ||
                this == EQUAL_TO || this == NOT_EQUAL_TO || this == AND || this == OR ||
                this == ASSIGNMENT ||
                this == QUESTION;
    }


    @Override
    public String toString() {
        return value == null || value.isBlank() ? this.name() : "[" + value + "]";
    }

    @Override
    public TokenType type() {
        return this;
    }

    @Override
    public TokenParams params() {
        return NoneParams.NONE;
    }
}
