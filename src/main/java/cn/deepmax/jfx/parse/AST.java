package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.lexer.Tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AST {


    public static class Program extends Base {
        @Override
        public String prettyString() {
            return "Program";
        }
    }

    public static class FunctionDefinition extends Base {
        @Override
        public String prettyString() {
            return "Function";
        }
    }

    public static class Statement extends Base {
        @Override
        public String prettyString() {
            return "Statement";
        }
    }

    public static class Exp extends Base {
        @Override
        public String prettyString() {
            return "Exp";
        }
    }

    public static class Identifier extends NoChildAstNode {
        public final Tokens.Id id;

        public Identifier(Tokens.Id id) {
            this.id = id;
        }

        @Override
        public String prettyString() {
            return id.params().toString();
        }
    }

    public static class IntExp extends Exp {
        public final Tokens.Constant intConstant;

        public IntExp(Tokens.Constant intConstant) {
            this.intConstant = intConstant;
        }

        @Override
        public String prettyString() {
            return String.format("Constant(%s)", intConstant.params());
        }
    }


    private abstract static class NoChildAstNode implements AstNode {
        @Override
        public List<AstNode> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    public abstract static class Base implements AstNode {
        private List<AstNode> children = new ArrayList<>();

        public void add(AstNode c) {
            children.add(c);
        }

        @Override
        public List<AstNode> getChildren() {
            return children;
        }

        @Override
        public String toString() {
            List<AstNode> ch = getChildren();
            StringBuilder sb = new StringBuilder();
            for (AstNode it : ch) {
                sb.append("\t").append(it.prettyString()).append("\n");
            }
            return String.format("%s\n%s", this.prettyString(), sb.toString());
        }
    }
}
