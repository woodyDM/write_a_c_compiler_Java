package cn.deepmax.jfx;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Lexer {

    private final byte[] data;
    private final int len;
    private int pos = 0;

    public Lexer(String s) {
        this.data = s.getBytes(StandardCharsets.UTF_8);
        this.len = this.data.length;
    }

    public Token nextToken() {
        while (pos < len && isWhitespace(data[pos])) {
            pos++;
        }
        if (pos >= len) return null;
        if (isSymbol(data[pos])) {
            byte b = data[pos];
            Token r = null;
            switch (b) {
                case '(' -> r = new Tokens.OpenParenthesis();
                case ')' -> r = new Tokens.CloseParenthesis();
                case '{' -> r = new Tokens.OpenBrace();
                case '}' -> r = new Tokens.CloseBrace();
                case ';' -> r = new Tokens.Semicolon();
                default -> throw new UnsupportedOperationException("invalid symbol " + b);
            }
            pos++;
            return r;
        }
        byte start = data[pos];
        String value = readUntil(b -> isWhitespace(b) || isSymbol(b));
        if ('0' <= start && start <= '9') {
            return new Tokens.Constant(value);
        }
        Set<String> KEYWORDS = Set.of("int", "void", "return");
        if (KEYWORDS.contains(value)) {
            return new Tokens.Keyword(value);
        }
        return new Tokens.Id(value);
    }

    interface BytePredicate {
        boolean test(byte b);
    }

    private String readUntil(BytePredicate until) {
        int start = pos;
        while (pos < len && !until.test(data[pos])) {
            pos++;
        }
        byte[] d = new byte[pos - start];
        System.arraycopy(data, start, d, 0, d.length);
        return new String(d, StandardCharsets.UTF_8);
    }

    static boolean isSymbol(byte b) {
        return b == '(' || b == ')' || b == '{' || b == '}' || b == ';';
    }

    static boolean isWhitespace(byte b) {
        return b == ' ' || b == '\t' || b == '\r' || b == '\n';
    }
}
