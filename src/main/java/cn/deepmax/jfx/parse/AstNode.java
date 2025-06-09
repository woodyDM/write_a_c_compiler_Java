package cn.deepmax.jfx.parse;

import java.util.List;

public interface AstNode {

    interface Program {
    }

    interface BlockItem {

    }

    /**
     * 函数参数列表
     */
    sealed interface Param permits Ast.VarParam {

    }

    interface Statement {
    }

    sealed interface Declaration permits Ast.FunctionDeclare, Ast.VarDeclare {

    }

    interface Exp {
    }

    interface Factor {

    }

    sealed interface ForInit permits Ast.ForInitDeclare, Ast.ForInitExp {

    }

    interface UnaryOperator {
    }

    interface BinaryOperator {
    }


    record SpecifierList(List<Specifier> list) {
        public static SpecifierList intList() {
            return new SpecifierList(List.of(Specifier.Int));
        }

        public Specifier getType() {
            return Specifier.Int;
        }

        public StorageClass getStorageClazz() {
            return list().stream()
                    .filter(i -> !i.type())
                    .findFirst()
                    .map(it -> it.storageClass)
                    .orElse(null);
        }

    }

    enum StorageClass {
        Static,
        Extern
    }

    enum Specifier {
        Int,
        Static(StorageClass.Static),
        Extern(StorageClass.Extern),
        ;

        public final StorageClass storageClass;

        Specifier() {
            this(null);
        }

        Specifier(StorageClass storageClass) {
            this.storageClass = storageClass;
        }

        public boolean type() {
            return storageClass == null;
        }

        public static Specifier parse(String s) {
            for (Specifier it : values()) {
                if (it.name().toLowerCase().equals(s)) return it;
            }
            return null;
        }
    }
}
