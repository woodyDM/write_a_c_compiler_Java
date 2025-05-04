package cn.deepmax.jfx.parse;

public class ParseException extends RuntimeException {

    public ParseException(String format, Object... args) {
        super(String.format(format, args));
    }
}
