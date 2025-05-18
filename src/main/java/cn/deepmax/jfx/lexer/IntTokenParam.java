package cn.deepmax.jfx.lexer;

public record IntTokenParam(int value) implements TokenParams {

    @Override
    public String toString() {
        return value + "";
    }

}
