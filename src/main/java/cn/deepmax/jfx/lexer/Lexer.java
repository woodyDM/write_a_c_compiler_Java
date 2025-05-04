package cn.deepmax.jfx.lexer;

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
            Token r;
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
        if (isDigit(start)) {
            if (allMatch(value, b -> isDigit(b))) {
                return new Tokens.Constant(value);
            } else {
                throw new IllegalStateException("invalid token " + value);
            }
        }

        Set<String> KEYWORDS = Set.of("int", "void", "return");
        if (KEYWORDS.contains(value)) {
            return new Tokens.Keyword(value);
        }

        boolean allW = allMatch(value, b -> isW(b));
        if (allW) {
            return new Tokens.Id(value);
        } else {
            throw new UnsupportedOperationException("invalid token " + value);
        }
    }

    interface BytePredicate {
        boolean test(byte b);
    }

    private static boolean allMatch(String s, BytePredicate fn) {
        if (s == null || s.isBlank()) {
            throw new IllegalStateException("invalid s");
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            if (!fn.test(b)) return false;
        }
        return true;
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

    static boolean isDigit(byte b) {
        return '0' <= b && b <= '9';
    }

    static boolean isAlpha(byte b) {
        return ('A' <= b && b <= 'Z') || ('a' <= b && b <= 'z');
    }

    static boolean isW(byte b) {
        return isDigit(b) || isAlpha(b) || b == '_';
    }

    static boolean isWhitespace(byte b) {
        return b == ' ' || b == '\t' || b == '\r' || b == '\n';
    }
}
