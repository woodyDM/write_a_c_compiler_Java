package cn.deepmax.jfx.lexer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Lexer {

    final byte[] data;
    final String txt;
    private final int len;
    int pos = 0;

    public Lexer(String s) {
        txt = s;
        this.data = s.getBytes(StandardCharsets.UTF_8);
        this.len = this.data.length;
    }

    String nearSubString() {
        int max = Math.min(len, pos + 10);
        return txt.substring(pos, max);
    }

    public List<Token> tokenList() {
        List<Token> r = new ArrayList<>();
        Token t;
        while (true) {
            t = nextToken();
            r.add(t);
            if (t == TokenType.EOF) break;
        }
        return r;
    }

    private Token parseNextTwo(char next, Token single, Token two) {
        if (pos + 1 < len && data[pos + 1] == next) {
            pos++;
            return two;
        } else {
            return single;
        }
    }

    public Token nextToken() {
        while (pos < len && isWhitespace(data[pos])) {
            pos++;
        }
        if (pos >= len) return TokenType.EOF;
        byte cb = data[pos];
        if (isSymbol(cb) || isOperand(cb) || isLogic(cb)) {
            Token r = switch (cb) {
                case '(' -> TokenType.OPEN_PARENTHESIS;
                case ')' -> TokenType.CLOSE_PARENTHESIS;
                case '{' -> TokenType.OPEN_BRACE;
                case '}' -> TokenType.CLOSE_BRACE;
                case ';' -> TokenType.SEMICOLON;
                case '~' -> TokenType.BITWISE;

                case '+' -> TokenType.PLUS;
                case '*' -> TokenType.MULTIP;
                case '/' -> TokenType.DIV;
                case '%' -> TokenType.REMINDER;

                case '-' -> parseNextTwo('-', TokenType.NEG, TokenType.DECREMENT);

                case '&' -> parseNextTwo('&', null, TokenType.AND);
                case '!' -> parseNextTwo('=', TokenType.NOT, TokenType.NOT_EQUAL_TO);
                case '|' -> parseNextTwo('|', null, TokenType.OR);
                case '=' -> parseNextTwo('=', TokenType.ASSIGNMENT, TokenType.EQUAL_TO);
                case '<' -> parseNextTwo('=', TokenType.LESS_THAN, TokenType.LESS_THAN_OR_EQ);
                case '>' -> parseNextTwo('=', TokenType.GREATER_THAN, TokenType.GREATER_THAN_OR_EQ);

                case '?' -> TokenType.QUESTION;
                case ':' -> TokenType.COLON;
                default -> throw new LexerException(this, "invalid symbol " + cb);
            };
            pos++;
            if (r == null) {
                throw new UnsupportedOperationException("invalid byte " + (char) cb);
            }
            return r;
        }
        String value = readUntil(b -> isWhitespace(b) || isSymbol(b) || isOperand(b) || isLogic(b));
        if (isDigit(cb)) {
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

    private boolean allMatch(String s, BytePredicate fn) {
        if (s == null || s.isBlank()) {
            throw new LexerException(this, "empty input string");
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

    static boolean isOperand(byte b) {
        return b == '~' || b == '-' || b == '+' || b == '*' || b == '/' || b == '%';
    }

    static boolean isLogic(byte b) {
        return b == '!' || b == '&' || b == '|' || b == '<' || b == '>' || b == '=';
    }

    static boolean isSymbol(byte b) {
        return b == '(' || b == ')' || b == '{' || b == '}' || b == ';' || b == '?' || b == ':';
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
