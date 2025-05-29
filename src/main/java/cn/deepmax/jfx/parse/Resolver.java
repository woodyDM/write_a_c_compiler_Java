package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.exception.SemanticException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * semantic resolve
 */
public class Resolver {

    private Identifiers identifiers = new Identifiers();
    private Labels currentLabel = new Labels();

    /**
     * resolve program
     *
     * @param program
     * @return
     */
    public Ast.AstProgram resolveProgram(Ast.AstProgram program) {
        List<Ast.FunctionDeclare> list = program.functionDeclarations()
                .stream()
                .map(f -> resolveFunctionDeclare(f))
                .toList();
        return new Ast.AstProgram(list);
    }

    private Ast.FunctionDeclare resolveFunctionDeclare(Ast.FunctionDeclare fun) {
        return (Ast.FunctionDeclare) resolveFunctionDeclaration(fun);
    }

    private Ast.Block resolveBlock(Ast.Block block) {
        if (block == null) {
            return null;
        }
        List<AstNode.BlockItem> list = block.blockItems()
                .stream()
                .map(this::resolveBlockItem)
                .map(this::labelBlockItem)
                .toList();
        return new Ast.Block(list);
    }

    private AstNode.BlockItem resolveBlockItem(AstNode.BlockItem item) {
        return switch (item) {
            case Ast.DeclareBlockItem d -> new Ast.DeclareBlockItem(resolveDeclaration(d.statement()));
            case Ast.StatementBlockItem i -> new Ast.StatementBlockItem(resolveStatement(i.statement()));
            default -> throw new SemanticException("unsupported item " + item.toString());
        };
    }

    private AstNode.BlockItem labelBlockItem(AstNode.BlockItem item) {
        return switch (item) {
            case Ast.DeclareBlockItem d -> d;
            case Ast.StatementBlockItem i -> new Ast.StatementBlockItem(labelStatement(i.statement()));
            default -> throw new SemanticException("unsupported item " + item.toString());
        };
    }


    private AstNode.Declaration resolveDeclaration(AstNode.Declaration declaration) {
        return switch (declaration) {
            case Ast.VarDeclare d -> {
                var idValue = d.identifier();
                identifiers.checkVar(idValue);
                String replacedName = identifiers.putVar(idValue, true);
                yield new Ast.VarDeclare(replacedName, resolveExp(d.exp()));
            }
            case Ast.FunctionDeclare f -> resolveFunctionDeclaration(f);
        };
    }

    private AstNode.Declaration resolveFunctionDeclaration(Ast.FunctionDeclare f) {
        identifiers.putFunc(f.identifier(), f);
        this.identifiers = this.identifiers.newScope();
        List<AstNode.Param> resolvedParams = f.params().stream()
                .map(this::resolveParam)
                .toList();
        Ast.Block newBody = resolveBlock(f.body());
        this.identifiers = this.identifiers.parent;
        return new Ast.FunctionDeclare(f.identifier(), resolvedParams, newBody);
    }

    private AstNode.Param resolveParam(AstNode.Param param) {
        if (param instanceof Ast.VarParam v) {
            identifiers.checkVar(v.identifier());
            String newId = identifiers.putVar(v.identifier(), true);
            return new Ast.VarParam(v.type(), newId);
        } else {
            return param;
        }
    }


    private AstNode.Factor resolveFactor(AstNode.Factor factor) {
        return switch (factor) {
            case Ast.ExpFactor e -> new Ast.ExpFactor(resolveExp(e.exp()));
            case Ast.Unary u -> new Ast.Unary(u.operator(), resolveFactor(u.factor()));
            case Ast.IntConstantFactor f -> f;
            case Ast.FunctionCall call -> {
                identifiers.checkFunCallName(call.identifier());
                yield new Ast.FunctionCall(
                        call.identifier(),
                        call.args().stream().map(this::resolveExp).toList()
                );
            }
            default -> throw new SemanticException("Unsupported " + factor);
        };
    }

    private AstNode.Exp resolveExp(AstNode.Exp exp) {
        return switch (exp) {
            case null -> null; //for declare
            case Ast.Assignment it -> {
                if (it.left() instanceof Ast.Var v) {
                    yield new Ast.Assignment(resolveExp(v), resolveExp(it.right()));
                } else {
                    throw new SemanticException("Invalid lvalue [%s]", it.left().toString());
                }
            }
            case Ast.Var v -> {
                String rawId = v.identifier();
                String existReplacement = identifiers.mappingToReplacement(rawId);
                if (existReplacement != null) {
                    yield new Ast.Var(existReplacement);
                } else {
                    throw new SemanticException("Undeclared variable [%s]", rawId);
                }
            }
            case Ast.Binary b -> {
                yield new Ast.Binary(b.operator(), resolveExp(b.left()), resolveExp(b.right()));
            }
            case Ast.FactorExp f -> {
                yield new Ast.FactorExp(resolveFactor(f.factor()));
            }
            case Ast.Conditional c -> {
                yield new Ast.Conditional(resolveExp(c.condition()),
                        resolveExp(c.trueExp()),
                        resolveExp(c.falseExp()));
            }
            default -> throw new UnsupportedOperationException(exp.toString());
        };
    }


    private AstNode.Statement resolveStatement(AstNode.Statement statement) {
        return switch (statement) {
            case null -> null;
            case Ast.ReturnStatement r -> new Ast.ReturnStatement(resolveExp(r.exp()));
            case Ast.Expression e -> new Ast.Expression(resolveExp(e.exp()));
            case Ast.Null n -> n;
            case Ast.If s -> new Ast.If(resolveExp(s.condition()),
                    resolveStatement(s.then()),
                    resolveStatement(s.elseSt()));
            case Ast.Compound c -> {
                this.identifiers = this.identifiers.newScope();
                List<AstNode.BlockItem> list = c.block().blockItems()
                        .stream().map(s -> resolveBlockItem(s))
                        .collect(Collectors.toList());
                var cp = new Ast.Compound(new Ast.Block(list));
                this.identifiers = this.identifiers.parent;
                yield cp;
            }
            case Ast.While w -> new Ast.While(resolveExp(w.condition()),
                    resolveStatement(w.body()));
            case Ast.DoWhile w -> new Ast.DoWhile(resolveStatement(w.body()),
                    resolveExp(w.condition()));
            case Ast.Break b -> b;
            case Ast.Continue c -> c;
            case Ast.BreakLabel l -> l;
            case Ast.ContinueLabel l -> l;
            case Ast.For f -> {
                this.identifiers = this.identifiers.newScope();

                var rf = new Ast.For(
                        resolveForInit(f.init()),
                        resolveExp(f.condition()),
                        resolveExp(f.post()),
                        resolveStatement(f.body())
                );
                this.identifiers = this.identifiers.parent;

                yield rf;
            }
            default -> throw new SemanticException("Unsupported " + statement.toString());
        };
    }

    private AstNode.Statement labelStatement(AstNode.Statement statement) {
        return switch (statement) {
            case null -> null;
            case Ast.Break it -> {
                if (this.currentLabel.noLabel()) {
                    throw new SemanticException("break statement outside of loop!");
                }
                yield new Ast.AnnotationLabeledStatement(it, this.currentLabel.labelValue);
            }
            case Ast.Continue it -> {
                if (this.currentLabel.noLabel()) {
                    throw new SemanticException("continue statement outside of loop!");
                }
                yield new Ast.AnnotationLabeledStatement(it, this.currentLabel.labelValue);
            }
            case Ast.While w -> {
                this.currentLabel = this.currentLabel.makeTempLabel();
                var labelBody = labelStatement(w.body());
                var w2 = new Ast.While(w.condition(), labelBody);
                var lw = new Ast.AnnotationLabeledStatement(w2, this.currentLabel.labelValue);
                this.currentLabel = this.currentLabel.parent;
                yield lw;
            }
            case Ast.DoWhile w -> {
                this.currentLabel = this.currentLabel.makeTempLabel();
                var labelBody = labelStatement(w.body());
                var w2 = new Ast.DoWhile(labelBody, w.condition());
                var lw = new Ast.AnnotationLabeledStatement(w2, this.currentLabel.labelValue);
                this.currentLabel = this.currentLabel.parent;
                yield lw;
            }
            case Ast.For f -> {
                this.currentLabel = this.currentLabel.makeTempLabel();
                var labelBody = labelStatement(f.body());
                var f2 = new Ast.For(f.init(), f.condition(), f.post(), labelBody);
                var lf = new Ast.AnnotationLabeledStatement(f2, this.currentLabel.labelValue);
                this.currentLabel = this.currentLabel.parent;
                yield lf;
            }
            case Ast.If f -> new Ast.If(
                    f.condition(),
                    labelStatement(f.then()),
                    labelStatement(f.elseSt())
            );
            case Ast.Compound c -> {
                List<AstNode.BlockItem> list = c.block().blockItems()
                        .stream()
                        .map(this::labelBlockItem)
                        .toList();
                yield new Ast.Compound(new Ast.Block(list));
            }
            default -> statement;
        };
    }

    private AstNode.ForInit resolveForInit(AstNode.ForInit init) {
        return switch (init) {
            case Ast.ForInitDeclare d -> new Ast.ForInitDeclare(
                    resolveDeclaration(d.declaration())
            );
            case Ast.ForInitExp e -> new Ast.ForInitExp(resolveExp(e.exp()));
        };
    }

    private <T> T fail(String msg) {
        throw new SemanticException(msg);
    }


}
