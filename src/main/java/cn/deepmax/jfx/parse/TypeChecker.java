package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.exception.SemanticException;
import cn.deepmax.jfx.utils.Assertion;

import java.util.Objects;

public class TypeChecker {

    private final SymbolTable table = new SymbolTable();

    /**
     * check program
     *
     * @param program
     * @return
     */
    public void checkProgram(Ast.AstProgram program) {
        program.functionDeclarations()
                .forEach(f -> checkFunctionDeclare(f));
    }

    private void checkFunctionDeclare(Ast.FunctionDeclare fun) {
        checkFunctionDeclaration(fun);
    }

    private void checkBlock(Ast.Block block) {
        Objects.requireNonNull(block);
        block.blockItems().forEach(bkitem -> checkBlockItem(bkitem));
    }

    private void checkBlockItem(AstNode.BlockItem item) {
        switch (item) {
            case Ast.DeclareBlockItem d -> checkDeclaration(d.statement());
            case Ast.StatementBlockItem i -> checkStatement(i.statement());
            default -> throw new SemanticException("unsupported item " + item.toString());
        }
    }

    private void checkDeclaration(AstNode.Declaration declaration) {
        switch (declaration) {
            case Ast.VarDeclare d -> {
                var idValue = d.identifier();
                table.put(idValue, TypeDef.VariableType.Int);
                checkExp(d.exp());
            }
            case Ast.FunctionDeclare f -> checkFunctionDeclaration(f);
        }
    }

    private void checkFunctionDeclaration(Ast.FunctionDeclare f) {
        var hasBody = f.body() != null;
        boolean alreadyDefined = false;

        var exist = table.get(f.identifier());

        if (exist != null) {
            if (!(exist instanceof TypeDef.FunType oldDef)) {
                throw new SemanticException("declaration is incompatible with previous :" + f.identifier());
            }
            if (oldDef.paramCount() != f.realParamSize()) {
                throw new SemanticException("declaration is incompatible with previous :" + f.identifier());
            }
            alreadyDefined = oldDef.defined();
            if (alreadyDefined && hasBody) {
                throw new SemanticException("Duplicate declaration of function " + f.identifier());
            }
        }
        table.put(f.identifier(), new TypeDef.FunType(f.realParamSize(), hasBody || alreadyDefined));
        if (hasBody) {
            f.params().forEach(p -> {
                if (p instanceof Ast.VarParam vp) {
                    table.put(vp.identifier(), TypeDef.VariableType.Int);
                } else {
                    throw new SemanticException("invalid param");
                }
            });
            checkBlock(f.body());
        }

    }


    private void checkFactor(AstNode.Factor factor) {
        switch (factor) {
            case Ast.ExpFactor e -> checkExp(e.exp());
            case Ast.Unary u -> checkFactor(u.factor());
            case Ast.FunctionCall call -> {
                String id = call.identifier();
                TypeDef.Type func = table.get(id);
                switch (func) {
                    case null -> throw new SemanticException("Can't call on undeclared function:" + id);
                    case TypeDef.VariableType t -> throw new SemanticException("Can't do function call on var " + id);
                    case TypeDef.FunType f -> {
                        if (f.paramCount() != call.args().size()) {
                            throw new SemanticException(String.format("function call need %d args, but only provide %d.",
                                    f.paramCount(),
                                    call.args().size()));
                        }
                        for (AstNode.Exp arg : call.args()) {
                            checkExp(arg);
                        }
                    }
                }
            }
            case Ast.IntConstantFactor i -> {
            }
            default -> throw new UnsupportedOperationException(factor.toString());
        }
    }

    private void checkExp(AstNode.Exp exp) {
        switch (exp) {
            case null -> {
            }
            case Ast.Assignment it -> {
                if (it.left() instanceof Ast.Var v) {
                    checkExp(v);
                    checkExp(it.right());
                } else {
                    throw new SemanticException("Invalid lvalue [%s]", it.left().toString());
                }
            }
            case Ast.Var v -> {
                String rawId = v.identifier();
                TypeDef.Type existType = table.get(rawId);
                Assertion.notNull(existType);
                if (existType != TypeDef.VariableType.Int) {
                    throw new SemanticException("Function name used as variable");
                }
            }
            case Ast.Binary b -> {
                checkExp(b.left());
                checkExp(b.right());
            }
            case Ast.FactorExp f -> {
                checkFactor(f.factor());
            }
            case Ast.Conditional c -> {
                checkExp(c.condition());
                checkExp(c.trueExp());
                checkExp(c.falseExp());
            }
            default -> throw new UnsupportedOperationException(exp.toString());
        }
    }


    private void checkStatement(AstNode.Statement statement) {
        switch (statement) {
            case null-> {
                return;
            }
            case Ast.ReturnStatement r -> checkExp(r.exp());
            case Ast.Expression e -> checkExp(e.exp());

            case Ast.If s -> {

                checkExp(s.condition());
                checkStatement(s.then());
                checkStatement(s.elseSt());
            }
            case Ast.Compound c -> c.block().blockItems().forEach(s -> checkBlockItem(s));
            case Ast.While w -> {
                checkExp(w.condition());
                checkStatement(w.body());
            }
            case Ast.DoWhile w -> {
                checkStatement(w.body());
                checkExp(w.condition());
            }

            case Ast.For f -> {
                checkForInit(f.init());
                checkExp(f.condition());
                checkExp(f.post());
                checkStatement(f.body());
            }
            default -> {
            }
        }
    }

    private void checkForInit(AstNode.ForInit init) {
        switch (init) {
            case Ast.ForInitDeclare d -> checkDeclaration(d.declaration());
            case Ast.ForInitExp e -> checkExp(e.exp());
        }
    }
}
