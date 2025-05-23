package cn.deepmax.jfx.exception;

public class SemanticException extends RuntimeException {

    public SemanticException(String msg, Object... args) {
        super(String.format(msg, args));
    }

}
