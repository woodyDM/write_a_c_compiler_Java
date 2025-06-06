package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.exception.SemanticException;
import cn.deepmax.jfx.utils.Assertion;

import java.util.Objects;

public class TypeChecker {

    private final SymbolTable table = new SymbolTable();

    public SymbolTable symbolTable() {
        return table;
    }

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

    private void checkBlock(String funIdentifier, Ast.Block block) {
        Objects.requireNonNull(block);
        block.blockItems().forEach(bkitem -> checkBlockItem(funIdentifier, bkitem));
    }

    private void checkBlockItem(String funIdentifier, AstNode.BlockItem item) {
        switch (item) {
            case Ast.DeclareBlockItem d -> checkDeclaration(funIdentifier, d.statement());
            case Ast.StatementBlockItem i -> checkStatement(funIdentifier, i.statement());
            default -> throw new SemanticException("unsupported item " + item.toString());
        }
    }

    private void checkDeclaration(String funIdentifier, AstNode.Declaration declaration) {
        switch (declaration) {
            case Ast.VarDeclare d -> {
                var idValue = d.identifier();
                table.putVariable(funIdentifier, idValue, TypeDef.VariableType.Int);
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
            if (oldDef.paramCount != f.realParamSize()) {
                throw new SemanticException("declaration is incompatible with previous :" + f.identifier());
            }
            alreadyDefined = oldDef.defined;
            if (alreadyDefined && hasBody) {
                throw new SemanticException("Duplicate declaration of function " + f.identifier());
            }
        }
        table.put(f.identifier(), TypeDef.FunType.newInstanceFrom(f.realParamSize(), hasBody || alreadyDefined, exist));
        if (hasBody) {
            f.params().forEach(p -> {
                if (p instanceof Ast.VarParam vp) {
                    table.putVariable(f.identifier(), vp.identifier(), TypeDef.VariableType.Int);
                } else {
                    throw new SemanticException("invalid param");
                }
            });
            checkBlock(f.identifier(), f.body());
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
                        if (f.paramCount != call.args().size()) {
                            throw new SemanticException(String.format("function call need %d args, but only provide %d.",
                                    f.paramCount,
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


    private void checkStatement(String funcIdentifier, AstNode.Statement statement) {
        switch (statement) {
            case null -> {
                return;
            }
            case Ast.ReturnStatement r -> checkExp(r.exp());
            case Ast.Expression e -> checkExp(e.exp());

            case Ast.If s -> {
                checkExp(s.condition());
                checkStatement(funcIdentifier, s.then());
                checkStatement(funcIdentifier, s.elseSt());
            }
            case Ast.Compound c -> c.block().blockItems().forEach(s -> checkBlockItem(funcIdentifier, s));
            case Ast.While w -> {
                checkExp(w.condition());
                checkStatement(funcIdentifier, w.body());
            }
            case Ast.DoWhile w -> {
                checkStatement(funcIdentifier, w.body());
                checkExp(w.condition());
            }

            case Ast.For f -> {
                checkForInit(funcIdentifier, f.init());
                checkExp(f.condition());
                checkExp(f.post());
                checkStatement(funcIdentifier, f.body());
            }
            default -> {
            }
        }
    }

    private void checkForInit(String identifier, AstNode.ForInit init) {
        switch (init) {
            case Ast.ForInitDeclare d -> checkDeclaration(identifier, d.declaration());
            case Ast.ForInitExp e -> checkExp(e.exp());
        }
    }
}
