package cn.deepmax.jfx.lexer;

import java.util.Objects;

public class StringTokenParam implements TokenParams {
    private final String value;

    public StringTokenParam(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        return obj instanceof StringTokenParam && Objects.equals(((StringTokenParam) obj).value, this.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
