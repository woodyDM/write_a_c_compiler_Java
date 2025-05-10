package cn.deepmax.jfx.lexer;

public class Tokens {
    private Tokens() {
    }

    public static class Id extends StringBase {
        public Id(String name) {
            super(TokenType.ID, name);
        }
    }

    public static class Constant extends StringBase {
        public Constant(String value) {
            super(TokenType.CONSTANT, value);
        }

    }

    public static class Keyword extends StringBase {

        public Keyword(String value) {
            super(TokenType.KEYWORD, value);
        }

    }

    public static class OpenParenthesis extends Base {
        public OpenParenthesis() {
            super(TokenType.OPEN_PARENTHESIS);
        }
    }

    public static class CloseParenthesis extends Base {
        public CloseParenthesis() {
            super(TokenType.CLOSE_PARENTHESIS);
        }
    }

    public static class OpenBrace extends Base {
        public OpenBrace() {
            super(TokenType.OPEN_BRACE);
        }
    }


    public static class CloseBrace extends Base {
        public CloseBrace() {
            super(TokenType.CLOSE_BRACE);
        }
    }


    public static class Semicolon extends Base {
        public Semicolon() {
            super(TokenType.SEMICOLON);
        }
    }

    public static class Bitwise extends Base {
        public Bitwise() {
            super(TokenType.BITWISE);
        }
    }

    public static class Multi extends Base {
        public Multi() {
            super(TokenType.MULTIP);
        }
    }

    public static class Divide extends Base {
        public Divide() {
            super(TokenType.DIV);
        }
    }

    public static class Remainder extends Base {
        public Remainder() {
            super(TokenType.REMINDER);
        }
    }

    public static class Plus extends Base {
        public Plus() {
            super(TokenType.PLUS);
        }
    }

    public static class Not extends Base {
        public Not() {
            super(TokenType.NOT);
        }
    }

    public static class And extends Base {
        public And() {
            super(TokenType.AND);
        }
    }

    public static class Or extends Base {
        public Or() {
            super(TokenType.OR);
        }
    }

    public static class EqualTo extends Base {
        public EqualTo() {
            super(TokenType.EQUAL_TO);
        }
    }

    public static class NotEqualTo extends Base {
        public NotEqualTo() {
            super(TokenType.NOT_EQUAL_TO);
        }
    }

    public static class LessThan extends Base {
        public LessThan() {
            super(TokenType.LESS_THAN);
        }
    }

    public static class LessThanOrEq extends Base {
        public LessThanOrEq() {
            super(TokenType.LESS_THAN_OR_EQ);
        }
    }

    public static class GreaterThan extends Base {
        public GreaterThan() {
            super(TokenType.GREATER_THAN);
        }
    }

    public static class GreaterThanOrEq extends Base {
        public GreaterThanOrEq() {
            super(TokenType.GREATER_THAN_OR_EQ);
        }
    }


    public static class Neg extends Base {
        public Neg() {
            super(TokenType.NEG);
        }
    }

    public static class Decrement extends Base {
        public Decrement() {
            super(TokenType.DECREMENT);
        }
    }

    public static class EOF extends Base {
        public static final EOF INS = new EOF();

        public EOF() {
            super(TokenType.EOF);
        }
    }

    public static class StringBase extends Base {
        private final StringTokenParam value;

        public StringBase(TokenType type, String value) {
            super(type);
            this.value = new StringTokenParam(value);
        }

        @Override
        public TokenParams params() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s[%s]", this.getClass().getSimpleName(), value.toString());
        }
    }

    public static class Base implements Token {
        public final TokenType type;

        public Base(TokenType type) {
            this.type = type;
        }

        public TokenType type() {
            return type;
        }

        @Override
        public String toString() {
            return type.toString();
        }

        @Override
        public TokenParams params() {
            return NoneParams.NONE;
        }

    }

}
