package cn.deepmax.jfx.lexer;

public class Tokens {

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

    public static class Bitwise extends Base  {
        public Bitwise() {
            super(TokenType.BITWISE);
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
            return this.getClass().getSimpleName();
        }

        @Override
        public TokenParams params() {
            return NoneParams.NONE;
        }

    }

}
