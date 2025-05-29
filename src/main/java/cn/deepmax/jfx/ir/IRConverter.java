package cn.deepmax.jfx.ir;

import cn.deepmax.jfx.exception.SemanticException;
import cn.deepmax.jfx.parse.Ast;
import cn.deepmax.jfx.parse.AstNode;
import cn.deepmax.jfx.parse.Labels;

import java.util.ArrayList;
import java.util.List;

public class IRConverter {

    private AstNode.Program program;

    public IRConverter(AstNode.Program program) {
        this.program = program;
    }

    public IR.Program convertToIR() {
        Ast.AstProgram p = (Ast.AstProgram) program;
        List<IR.FunctionDef> result = new ArrayList<>();
        for (Ast.FunctionDeclare fn : p.functionDeclarations()) {
            IR.FunctionDef irDef = convertFn(fn);
            if (irDef != null) {
                result.add(irDef);
            }
        }
        return new IRType.Program(result);
    }

    private IR.FunctionDef convertFn(Ast.FunctionDeclare fn) {
        if (fn.body() == null) {
            return null;
        }
        List<IR.Instruction> instructions = convertBlockItems(fn.body().blockItems());
        //todo?
        return new IRType.FunctionDef(fn.identifier(), null, instructions);
    }

    private List<IR.Instruction> convertBlockItems(List<AstNode.BlockItem> itemList) {
        List<IR.Instruction> list = new ArrayList<>();
        for (AstNode.BlockItem blockItem : itemList) {
            convertBlockItem(blockItem, list);
        }
        //always return 0
        list.add(new IRType.Return(new IRType.Constant(0)));
        return list;
    }

    private void convertBlockItem(AstNode.BlockItem blockItem, List<IR.Instruction> list) {
        switch (blockItem) {
            case Ast.DeclareBlockItem d -> {
                switch (d.statement()) {
                    case Ast.VarDeclare st -> {
                        Ast.VarDeclare statement = (Ast.VarDeclare) d.statement();
                        convertDeclare(statement, list);
                    }
                    case Ast.FunctionDeclare st -> {
                        //ignore since we can't define function in block.
                    }
                }

            }
            case Ast.StatementBlockItem stmt -> {
                convertStatement(stmt.statement(), list);
            }
            default -> throw new UnsupportedOperationException("invalid block item " + blockItem);
        }
    }

    private void convertDeclare(Ast.VarDeclare statement, List<IR.Instruction> list) {
        if (statement.exp() == null) {
            //no init ,so no tacky
            return;
        }
        var result = convertValue(statement.exp(), list);
        IRType.Var v = new IRType.Var(statement.identifier());
        list.add(new IRType.Copy(result, v));
    }

    private void convertStatement(AstNode.Statement statement, List<IR.Instruction> list) {
        switch (statement) {
            case Ast.ReturnStatement rs -> {
                AstNode.Exp exp = rs.exp();
                IRType.Return rt = new IRType.Return(convertValue(exp, list));
                list.add(rt);
            }
            case Ast.Expression exp -> {
                var _result = convertValue(exp.exp(), list);
                //emit the result
            }
            case Ast.Null n -> {
                //no instructions
            }
            case Ast.If s -> {
                String exitLabel = "if_exit_label." + IRType.Label.nextId();
                if (s.elseSt() == null) {
                    var condition = convertValue(s.condition(), list);
                    list.add(new IRType.JumpIfZero(condition, exitLabel));
                    convertStatement(s.then(), list);
                    list.add(new IRType.Label(exitLabel));
                } else {
                    String elseLabel = "if_else_label." + IRType.Label.nextId();
                    var condition = convertValue(s.condition(), list);
                    list.add(new IRType.JumpIfZero(condition, elseLabel));
                    convertStatement(s.then(), list);
                    list.add(new IRType.Jump(exitLabel));
                    list.add(new IRType.Label(elseLabel));
                    convertStatement(s.elseSt(), list);
                    list.add(new IRType.Label(exitLabel));
                }
            }
            case Ast.Compound c -> {
                for (AstNode.BlockItem blockItem : c.block().blockItems()) {
                    convertBlockItem(blockItem, list);
                }
            }
            case Ast.AnnotationLabeledStatement ano -> {
                String continueLabelOf = Labels.continueLabelOf(ano.label());
                String breakLabelOf = Labels.breakLabelOf(ano.label());
                switch (ano.statement()) {
                    case Ast.Break bk -> list.add(new IRType.Jump(breakLabelOf));
                    case Ast.Continue ct -> list.add(new IRType.Jump(continueLabelOf));
                    case Ast.DoWhile w -> {
                        String startLabel = "dowhile_start_" + IRType.Label.nextId();
                        list.add(new IRType.Label(startLabel));
                        convertStatement(w.body(), list);
                        list.add(new IRType.Label(continueLabelOf));
                        var conditionResult = convertValue(w.condition(), list);
                        list.add(new IRType.JumpIfNotZero(conditionResult, startLabel));
                        list.add(new IRType.Label(breakLabelOf));
                    }
                    case Ast.While w -> {
                        list.add(new IRType.Label(continueLabelOf));
                        var conditionResult = convertValue(w.condition(), list);
                        list.add(new IRType.JumpIfZero(conditionResult, breakLabelOf));
                        convertStatement(w.body(), list);
                        list.add(new IRType.Jump(continueLabelOf));
                        list.add(new IRType.Label(breakLabelOf));
                    }
                    case Ast.For f -> {
                        convertForInit(f.init(), list);
                        String startLabel = "for_start_" + IRType.Label.nextId();
                        list.add(new IRType.Label(startLabel));
                        var conditionV = f.condition() == null ? new IRType.Constant(1) : convertValue(f.condition(), list);
                        list.add(new IRType.JumpIfZero(conditionV, breakLabelOf));
                        convertStatement(f.body(), list);
                        list.add(new IRType.Label(continueLabelOf));
                        if (f.post() != null) convertValue(f.post(), list);
                        list.add(new IRType.Jump(startLabel));
                        list.add(new IRType.Label(breakLabelOf));
                    }

                    default -> throw new UnsupportedOperationException(ano.statement().toString());
                }
            }
            default -> throw new UnsupportedOperationException(statement.toString());
        }
    }

    private void convertForInit(AstNode.ForInit init, List<IR.Instruction> list) {
        switch (init) {
            case Ast.ForInitDeclare d -> {
                Ast.VarDeclare dd = (Ast.VarDeclare) d.declaration();
                convertDeclare(dd, list);
            }
            case Ast.ForInitExp e -> {
                if (e.exp() != null) {
                    convertValue(e.exp(), list);

                }
            }
            default -> throw new UnsupportedOperationException(init.toString());
        }
    }

    /**
     * emit_tacky
     *
     * @param exp
     * @param list
     * @return
     */
    private IR.Val convertValue(AstNode.Exp exp, List<IR.Instruction> list) {
        return switch (exp) {
            //convert factor here
            case Ast.FactorExp f -> switch (f.factor()) {
                case Ast.IntConstantFactor i -> new IRType.Constant(i.value());
                case Ast.Unary u -> {
                    var src = convertValue(new Ast.FactorExp(u.factor()), list);
                    var dst = IRType.Var.makeTemp();
                    var op = convertUnaryOp(u);
                    list.add(new IRType.Unary(op, src, dst));
                    yield dst;
                }
                case Ast.ExpFactor e -> convertValue(e.exp(), list);
                case Ast.FunctionCall call -> {
                    List<IR.Val> params = new ArrayList<>();
                    for (AstNode.Exp arg : call.args()) {
                        params.add(convertValue(arg, list));
                    }
                    var dst = IRType.Var.makeTemp();
                    var ins = new IRType.FunCall(
                            call.identifier(),
                            params,
                            dst
                    );
                    list.add(ins);
                    yield dst;
                }
                default -> throw new UnsupportedOperationException("invalid factor " + f.factor().toString());
            };
            case Ast.Binary b -> {
                var bop = b.operator();
                if (bop == Ast.BinaryOp.And) {
                    //should support short-circuit
                    String falseLabel = "and_false_label." + IRType.Label.nextId();
                    String exitLabel = "and_exit_label." + IRType.Label.nextId();
                    var dst = IRType.Var.makeTemp();

                    var v1 = convertValue(b.left(), list);
                    list.add(new IRType.JumpIfZero(v1, falseLabel));
                    var v2 = convertValue(b.right(), list);
                    list.add(new IRType.JumpIfZero(v2, falseLabel));
                    list.add(new IRType.Copy(new IRType.Constant(1), dst));
                    list.add(new IRType.Jump(exitLabel));
                    list.add(new IRType.Label(falseLabel));
                    list.add(new IRType.Copy(new IRType.Constant(0), dst));
                    list.add(new IRType.Label(exitLabel));

                    yield dst;
                } else if (bop == Ast.BinaryOp.Or) {
                    //should support short-circuit
                    String trueLabel = "or_true_label." + IRType.Label.nextId();
                    String exitLabel = "or_exit_label." + IRType.Label.nextId();
                    var dst = IRType.Var.makeTemp();

                    var v1 = convertValue(b.left(), list);
                    list.add(new IRType.JumpIfNotZero(v1, trueLabel));
                    var v2 = convertValue(b.right(), list);
                    list.add(new IRType.JumpIfNotZero(v2, trueLabel));
                    list.add(new IRType.Copy(new IRType.Constant(0), dst));
                    list.add(new IRType.Jump(exitLabel));
                    list.add(new IRType.Label(trueLabel));
                    list.add(new IRType.Copy(new IRType.Constant(1), dst));
                    list.add(new IRType.Label(exitLabel));

                    yield dst;
                } else {
                    //normal operators
                    var v1 = convertValue(b.left(), list);
                    var v2 = convertValue(b.right(), list);
                    var dst = IRType.Var.makeTemp();
                    IR.BinaryOperator op = convertBinaryOp(b.operator());
                    list.add(new IRType.Binary(op, v1, v2, dst));
                    yield dst;
                }
            }
            case Ast.Var v -> new IRType.Var(v.identifier());
            case Ast.Assignment ag -> {
                var result = convertValue(ag.right(), list);
                if (ag.left() instanceof Ast.Var v) {
                    IRType.Var dst = new IRType.Var(v.identifier());
                    list.add(new IRType.Copy(result, dst));
                    yield dst;
                } else {
                    throw new SemanticException("assignment left only support Var");
                }
            }
            case Ast.Conditional c -> {
                //should support short-circuit
                String falseLabel = "conditional_false_label." + IRType.Label.nextId();
                String exitLabel = "conditional_exit_label." + IRType.Label.nextId();
                var dst = IRType.Var.makeTemp();

                var condition = convertValue(c.condition(), list);
                list.add(new IRType.JumpIfZero(condition, falseLabel));
                var r = convertValue(c.trueExp(), list);
                list.add(new IRType.Copy(r, dst));
                list.add(new IRType.Jump(exitLabel));

                list.add(new IRType.Label(falseLabel));
                var rfalse = convertValue(c.falseExp(), list);
                list.add(new IRType.Copy(rfalse, dst));
                list.add(new IRType.Label(exitLabel));

                yield dst;
            }
            default -> throw new UnsupportedOperationException(exp.toString());
        };
    }

    private IR.BinaryOperator convertBinaryOp(AstNode.BinaryOperator operator) {
        return switch (operator) {
            case Ast.BinaryOp.Add -> IRType.BinaryOp.Add;
            case Ast.BinaryOp.Subtract -> IRType.BinaryOp.Subtract;
            case Ast.BinaryOp.Multiply -> IRType.BinaryOp.Multiply;
            case Ast.BinaryOp.Divide -> IRType.BinaryOp.Divide;
            case Ast.BinaryOp.Remainder -> IRType.BinaryOp.Remainder;

            case Ast.BinaryOp.Equal -> IRType.BinaryOp.Equal;
            case Ast.BinaryOp.NotEqual -> IRType.BinaryOp.NotEqual;
            case Ast.BinaryOp.And -> IRType.BinaryOp.And;
            case Ast.BinaryOp.Or -> IRType.BinaryOp.Or;
            case Ast.BinaryOp.LessThan -> IRType.BinaryOp.LessThan;
            case Ast.BinaryOp.LessOrEqual -> IRType.BinaryOp.LessOrEqual;
            case Ast.BinaryOp.GreaterThan -> IRType.BinaryOp.GreaterThan;
            case Ast.BinaryOp.GreaterOrEqual -> IRType.BinaryOp.GreaterOrEqual;
            default -> throw new UnsupportedOperationException("invalid binary op " + operator);
        };
    }

    private IR.UnaryOperator convertUnaryOp(Ast.Unary u) {
        return switch (u.operator()) {
            case Ast.UnaryOp.Complement -> IRType.UnaryOp.Complement;
            case Ast.UnaryOp.Negate -> IRType.UnaryOp.Negate;
            case Ast.UnaryOp.Not -> IRType.UnaryOp.Not;
            default -> throw new UnsupportedOperationException(u.toString());
        };
    }

}
