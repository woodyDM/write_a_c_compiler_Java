package cn.deepmax.jfx;

public class Tokens {

    public static class Id extends Base {
        public final String name;

        public Id(String name) {
            super(TokenType.ID);
            this.name = name;
        }

        @Override
        public String toString() {
            return "Id{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    '}';
        }
    }

    public static class Constant extends Base {
        public final String value;

        public Constant(String value) {
            super(TokenType.CONSTANT);
            this.value = value;
        }

        @Override
        public String toString() {
            return "Constant{" +
                    "value='" + value + '\'' +
                    ", type=" + type +
                    '}';
        }
    }

    public static class Keyword extends Base {
        public final String value;

        public Keyword(String value) {
            super(TokenType.KEYWORD);
            this.value = value;
        }

        @Override
        public String toString() {
            return "Keyword{" +
                    "value='" + value + '\'' +
                    ", type=" + type +
                    '}';
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
    }

}
