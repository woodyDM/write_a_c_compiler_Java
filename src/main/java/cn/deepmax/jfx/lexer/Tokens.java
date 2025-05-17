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

    public static class NewLine implements Token {
        public final int lineNumber;

        public NewLine(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        @Override
        public TokenType type() {
            return TokenType.NEWLINE;
        }

        @Override
        public TokenParams params() {
            return new IntTokenParam(this.lineNumber);
        }

        @Override
        public String toString() {
            return "NewLine(" + lineNumber + ")";
        }
    }

    public static class StringBase implements Token {
        public final TokenType type;
        private final StringTokenParam value;

        public StringBase(TokenType type, String value) {
            this.type = type;
            this.value = new StringTokenParam(value);
        }

        @Override
        public TokenType type() {
            return type;
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

}
