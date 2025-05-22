package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.exception.SemanticException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * semantic resolve
 */
class Resolver {

    private Variables variables = new Variables();
    private Labels currentLabel = new Labels();

    Ast.Block resolveBlock(Ast.Block block) {
        List<AstNode.BlockItem> list = block.blockItems()
                .stream().map(this::resolveBlockItem)
                .map(this::labelBlockItem)
                .toList();
        return new Ast.Block(list);
    }

    AstNode.BlockItem resolveBlockItem(AstNode.BlockItem item) {
        return switch (item) {
            case Ast.DeclareBlockItem d -> new Ast.DeclareBlockItem(resolveDeclaration(d.statement()));
            case Ast.StatementBlockItem i -> new Ast.StatementBlockItem(resolveStatement(i.statement()));
            default -> throw new SemanticException("unsupported item " + item.toString());
        };
    }

    AstNode.BlockItem labelBlockItem(AstNode.BlockItem item) {
        return switch (item) {
            case Ast.DeclareBlockItem d -> d;
            case Ast.StatementBlockItem i -> new Ast.StatementBlockItem(labelStatement(i.statement()));
            default -> throw new SemanticException("unsupported item " + item.toString());
        };
    }


    AstNode.Declaration resolveDeclaration(AstNode.Declaration declaration) {
        if (declaration instanceof Ast.VarDeclare d) {
            var idValue = d.identifier();
            boolean exist = variables.existInCurrentScope(idValue);
            if (exist) {
                throw new SemanticException("Duplicate variable declaration! id =" + idValue);
            }
            String replacedName = idValue + "." + Variables.nextId();
            variables.put(idValue, replacedName, true);
            return new Ast.VarDeclare(replacedName, resolveExp(d.exp()));
        }
        throw new UnsupportedOperationException(declaration.toString());
    }


    AstNode.Factor resolveFactor(AstNode.Factor factor) {
        return switch (factor) {
            case Ast.ExpFactor e -> new Ast.ExpFactor(resolveExp(e.exp()));
            case Ast.Unary u -> new Ast.Unary(u.operator(), resolveFactor(u.factor()));
            case Ast.IntConstantFactor f -> f;
            default -> throw new SemanticException("Unsupported " + factor);
        };
    }

    AstNode.Exp resolveExp(AstNode.Exp exp) {
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
                String existReplacement = variables.mappingToReplacement(rawId);
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


    AstNode.Statement resolveStatement(AstNode.Statement statement) {
        return switch (statement) {
            case null -> null;
            case Ast.ReturnStatement r -> new Ast.ReturnStatement(resolveExp(r.exp()));
            case Ast.Expression e -> new Ast.Expression(resolveExp(e.exp()));
            case Ast.Null n -> n;
            case Ast.If s -> new Ast.If(resolveExp(s.condition()),
                    resolveStatement(s.then()),
                    resolveStatement(s.elseSt()));
            case Ast.Compound c -> {
                this.variables = this.variables.newScope();
                List<AstNode.BlockItem> list = c.block().blockItems()
                        .stream().map(s -> resolveBlockItem(s))
                        .collect(Collectors.toList());
                var cp = new Ast.Compound(new Ast.Block(list));
                this.variables = this.variables.parent;
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
                this.variables = this.variables.newScope();

                var rf = new Ast.For(
                        resolveForInit(f.init()),
                        resolveExp(f.condition()),
                        resolveExp(f.post()),
                        resolveStatement(f.body())
                );
                this.variables = this.variables.parent;

                yield rf;
            }
            default -> throw new SemanticException("Unsupported " + statement.toString());
        };
    }

    AstNode.Statement labelStatement(AstNode.Statement statement) {
        return switch (statement) {
            case null -> null;
            case Ast.Break it -> {
                if (this.currentLabel.noLabel()) {
                    //fixme parser pos is at end!
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

    AstNode.ForInit resolveForInit(AstNode.ForInit init) {
        return switch (init) {
            case Ast.ForInitDeclare d -> new Ast.ForInitDeclare(
                    resolveDeclaration(d.declaration())
            );
            case Ast.ForInitExp e -> new Ast.ForInitExp(resolveExp(e.exp()));
            default -> throw new SemanticException("unsupported init " + init.toString());
        };
    }
}
