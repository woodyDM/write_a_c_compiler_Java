package cn.deepmax.jfx.lexer;

public class NoneParams implements TokenParams {

    public static final NoneParams NONE = new NoneParams();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return obj instanceof NoneParams;
    }

    @Override
    public String toString() {
        return "NoneParams";
    }
}
