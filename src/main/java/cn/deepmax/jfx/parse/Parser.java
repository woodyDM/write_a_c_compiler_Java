package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.lexer.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 主要是 recursive dscent parsing 解析
 * 中间有部分是： precedence climbing
 * 还有个技术是 pratt parsing ，听说和上面的是等价
 */
public class Parser {
    public final List<Token> tokenList;
    int pos = 0;
    private final int len;
    private Variables variables = new Variables();
    private Token currentLine;

    public Parser(Lexer lexer) {
        this.tokenList = lexer.tokenList();
        this.len = this.tokenList.size();
    }

    public Ast.AstProgram parseProgram() {
        Ast.FunctionDefinition fnDef = parseFunctionDefinition();
        Ast.AstProgram p = new Ast.AstProgram(fnDef);

        expect(TokenType.EOF, NoneParams.NONE);
        return p;
    }

    public Ast.AstProgram resolveProgram(Ast.AstProgram program) {
        List<AstNode.BlockItem> list = program.functionDefinition()
                .body().blockItems()
                .stream().map(this::resolveBlockItem)
                .toList();
        var resovledBlock = new Ast.Block(list);
        Ast.FunctionDefinition fnDef = new Ast.FunctionDefinition(
                program.functionDefinition().name(),
                resovledBlock
        );
        return new Ast.AstProgram(fnDef);
    }

    public Ast.FunctionDefinition parseFunctionDefinition() {

        expect(TokenType.KEYWORD, new StringTokenParam("int"));
        Token idToken = expect(TokenType.ID, null);
        String idName = idToken.params().toString();

        expect(TokenType.OPEN_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.KEYWORD, new StringTokenParam("void"));
        expect(TokenType.CLOSE_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.OPEN_BRACE, NoneParams.NONE);

        List<AstNode.BlockItem> fnBody = new ArrayList<>();
        parseFunctionBody(fnBody);

        expect(TokenType.CLOSE_BRACE, NoneParams.NONE);

        return new Ast.FunctionDefinition(idName, new Ast.Block(fnBody));
    }

    private void parseFunctionBody(List<AstNode.BlockItem> list) {
        while (getNextToken() != TokenType.CLOSE_BRACE) {
            AstNode.BlockItem nextItem = parseBlockItem();
            list.add(nextItem);
        }
    }

    //blockItem ::= <statement> | <declaration>
    private AstNode.BlockItem parseBlockItem() {
        Token nextToken = getNextToken();
        if (nextToken.isKeyword("int")) {
            //declaration
            moveNext();
            AstNode.Declaration declare = parseDeclaration();
            return new Ast.DeclareBlockItem(declare);
        }
        AstNode.Statement statement = parseStatement();
        return new Ast.StatementBlockItem(statement);
    }

    private AstNode.Declaration parseDeclaration() {
        Token id = expect(TokenType.ID, null);
        AstNode.Exp init = null;
        if (getNextToken() == TokenType.ASSIGNMENT) {
            //init
            moveNext();
            init = parseExp(0);
        }
        expect(TokenType.SEMICOLON);
        return new Ast.Declare(id.params().toString(), init);
    }

    private AstNode.BlockItem resolveBlockItem(AstNode.BlockItem item) {
        return switch (item) {
            case Ast.DeclareBlockItem d -> new Ast.DeclareBlockItem(resolveDeclaration(d.statement()));
            case Ast.StatementBlockItem i -> new Ast.StatementBlockItem(resolveStatement(i.statement()));
            default -> throw new ParseException(this, "unsupported item " + item.toString());
        };
    }

    private AstNode.Declaration resolveDeclaration(AstNode.Declaration declaration) {
        if (declaration instanceof Ast.Declare d) {
            var idValue = d.identifier();
            boolean exist = variables.existInCurrentScope(idValue);
            if (exist) {
                throw new ParseException(this, "Duplicate variable declaration! id =" + idValue);
            }
            String replacedName = idValue + "." + Variables.nextId();
            variables.put(idValue, replacedName, true);
            return new Ast.Declare(replacedName, resolveExp(d.exp()));
        }
        throw new UnsupportedOperationException(declaration.toString());
    }

    public AstNode.Statement parseStatement() {
        var nextToken = getNextToken();
        if (nextToken == TokenType.SEMICOLON) {
            moveNext();
            return new Ast.Null();
        }
        if (nextToken == TokenType.OPEN_BRACE) {
            moveNext();
            List<AstNode.BlockItem> list = new ArrayList<>();
            while (getNextToken() != TokenType.CLOSE_BRACE) {
                var it = parseBlockItem();
                list.add(it);
            }
            expect(TokenType.CLOSE_BRACE);
            return new Ast.Compound(new Ast.Block(list));
        }
        if (nextToken instanceof Tokens.Keyword kw) {
            moveNext();

            String value = kw.params().toString();
            switch (value) {
                case "return" -> {
                    AstNode.Exp node = parseExp(0);
                    Ast.ReturnStatement statement = new Ast.ReturnStatement(node);
                    expect(TokenType.SEMICOLON, NoneParams.NONE);
                    return statement;
                }
                case "if" -> {
                    expect(TokenType.OPEN_PARENTHESIS);
                    AstNode.Exp exp = parseExp(0);
                    expect(TokenType.CLOSE_PARENTHESIS);
                    var thenStmt = parseStatement();
                    Token elseIf = getNextToken();
                    AstNode.Statement elseSt;
                    if (elseIf.isKeyword("else")) {
                        moveNext();
                        elseSt = parseStatement();
                    } else {
                        elseSt = null;
                    }
                    return new Ast.If(exp, thenStmt, elseSt);
                }
                case "while" -> {
                    expect(TokenType.OPEN_PARENTHESIS);
                    AstNode.Exp exp = parseExp(0);
                    expect(TokenType.CLOSE_PARENTHESIS);
                    var whileBody = parseStatement();
                    return new Ast.While(exp, whileBody);
                }
                case "break" -> {
                    expect(TokenType.SEMICOLON);
                    return new Ast.Break();
                }
                case "continue" -> {
                    expect(TokenType.SEMICOLON);
                    return new Ast.Continue();
                }
                case "do" -> {
                    var doBody = parseStatement();
                    expect(TokenType.KEYWORD, new StringTokenParam("while"));
                    expect(TokenType.OPEN_PARENTHESIS);
                    AstNode.Exp exp = parseExp(0);
                    expect(TokenType.CLOSE_PARENTHESIS);
                    expect(TokenType.SEMICOLON);
                    return new Ast.DoWhile(doBody, exp);
                }
                case "for" -> {
                    expect(TokenType.OPEN_PARENTHESIS);
                    AstNode.ForInit forInit = parseForInit();
                    var conditionoExp = tryParseExp(TokenType.SEMICOLON);
                    var postExp = tryParseExp(TokenType.CLOSE_PARENTHESIS);
                    var body = parseStatement();
                    return new Ast.For(forInit, conditionoExp, postExp, body);
                }
                default -> throw new ParseException(this, "unsupported keyword " + nextToken);
            }
        }

        //normal exp
        AstNode.Exp node = parseExp(0);
        Ast.Expression statement = new Ast.Expression(node);
        expect(TokenType.SEMICOLON, NoneParams.NONE);
        return statement;
    }

    private AstNode.ForInit parseForInit() {
        Token nextToken = getNextToken();
        if (nextToken.isKeyword("int")) {
            //declare
            moveNext();
            var dec = parseDeclaration();
            return new Ast.ForInitDeclare(dec);
        }
        //exp ?
        var exp = tryParseExp(TokenType.SEMICOLON);
        return new Ast.ForInitExp(exp);
    }

    private AstNode.Exp tryParseExp(TokenType end) {
        Token nextToken = getNextToken();
        if (nextToken == end) {
            moveNext();
            return null;
        }
        var exp = parseExp(0);
        expect(end);
        return exp;
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
            case Ast.For f -> new Ast.For(
                    resolveForInit(f.init()),
                    resolveExp(f.condition()),
                    resolveExp(f.post()),
                    resolveStatement(f.body())
            );
            default -> throw new ParseException(this, "Unsupported " + statement.toString());
        };
    }

    private AstNode.ForInit resolveForInit(AstNode.ForInit init) {
        return switch (init) {
            case Ast.ForInitDeclare d -> new Ast.ForInitDeclare(
                    resolveDeclaration(d.declaration())
            );
            case Ast.ForInitExp e -> new Ast.ForInitExp(resolveExp(e.exp()));
            default -> throw new ParseException(this, "unsupported init " + init.toString());
        };
    }

    public AstNode.Factor parseFactor() {
        Token token = moveToNextToken();
        return switch (token.type()) {
            case CONSTANT -> {
                String v = token.params().toString();
                yield new Ast.IntConstantFactor(Integer.parseInt(v));
            }
            case BITWISE, NEG, NOT -> {
                AstNode.UnaryOperator op = parseOp(token);
                var innerFact = parseFactor();
                yield new Ast.Unary(op, innerFact);
            }
            case OPEN_PARENTHESIS -> {
                AstNode.Exp inner = parseExp(0);
                expect(TokenType.CLOSE_PARENTHESIS, NoneParams.NONE);
                yield new Ast.ExpFactor(inner);
            }
            case ID -> {
                Tokens.Id id = (Tokens.Id) token;
                Ast.Var exp = new Ast.Var(id.params().toString());
                yield new Ast.ExpFactor(exp);
            }
            default -> throw new ParseException(this, "Malformed factor:" + token.toString());
        };
    }

    public AstNode.Exp parseExp(int minPrec) {
        AstNode.Factor factor = parseFactor();
        AstNode.Exp left = factor instanceof Ast.ExpFactor(AstNode.Exp exp) ? exp : new Ast.FactorExp(factor);
        Token nextToken = getNextToken();
        while (nextToken.type().isBinaryOp() && nextToken.type().prec() >= minPrec) {
            moveNext();
            if (nextToken == TokenType.ASSIGNMENT) {
                //right-associative
                var right = parseExp(nextToken.type().prec());
                left = new Ast.Assignment(left, right);
            } else if (nextToken == TokenType.QUESTION) {
                var mid = parseConditionalExp();
                var right = parseExp(nextToken.type().prec());
                left = new Ast.Conditional(left, mid, right);
            } else {
                //binary left-associative
                var op = parseBinop(nextToken);
                AstNode.Exp right = parseExp(nextToken.type().prec() + 1);
                left = new Ast.Binary(op, left, right);
            }
            nextToken = getNextToken();
        }
        return left;
    }

    /**
     * parse conditional exp
     *
     * @return
     */
    private AstNode.Exp parseConditionalExp() {
        AstNode.Exp exp = parseExp(0);
        expect(TokenType.COLON);
        return exp;
    }

    private AstNode.Factor resolveFactor(AstNode.Factor factor) {
        return switch (factor) {
            case Ast.ExpFactor e -> new Ast.ExpFactor(resolveExp(e.exp()));
            case Ast.Unary u -> new Ast.Unary(u.operator(), resolveFactor(u.factor()));
            case Ast.IntConstantFactor f -> f;
            default -> throw new ParseException(this, "Unsupported " + factor);
        };
    }

    private AstNode.Exp resolveExp(AstNode.Exp exp) {
        return switch (exp) {
            case null -> null; //for declare
            case Ast.Assignment it -> {
                if (it.left() instanceof Ast.Var v) {
                    yield new Ast.Assignment(resolveExp(v), resolveExp(it.right()));
                } else {
                    throw new ParseException(this, "Invalid lvalue [%s]", it.left().toString());
                }
            }
            case Ast.Var v -> {
                String rawId = v.identifier();
                String existReplacement = variables.mappingToReplacement(rawId);
                if (existReplacement != null) {
                    yield new Ast.Var(existReplacement);
                } else {
                    throw new ParseException(this, "Undeclared variable [%s]", rawId);
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

    private AstNode.BinaryOperator parseBinop(Token token) {
        return switch (token) {
            case TokenType.PLUS -> Ast.BinaryOp.Add;
            case TokenType.NEG -> Ast.BinaryOp.Subtract;
            case TokenType.MULTIP -> Ast.BinaryOp.Multiply;
            case TokenType.DIV -> Ast.BinaryOp.Divide;
            case TokenType.REMINDER -> Ast.BinaryOp.Remainder;

            case TokenType.AND -> Ast.BinaryOp.And;
            case TokenType.OR -> Ast.BinaryOp.Or;
            case TokenType.EQUAL_TO -> Ast.BinaryOp.Equal;
            case TokenType.NOT_EQUAL_TO -> Ast.BinaryOp.NotEqual;
            case TokenType.LESS_THAN -> Ast.BinaryOp.LessThan;
            case TokenType.LESS_THAN_OR_EQ -> Ast.BinaryOp.LessOrEqual;
            case TokenType.GREATER_THAN -> Ast.BinaryOp.GreaterThan;
            case TokenType.GREATER_THAN_OR_EQ -> Ast.BinaryOp.GreaterOrEqual;

            default -> throw new UnsupportedOperationException("invalid token " + token.toString());
        };
    }


    private AstNode.UnaryOperator parseOp(Token token) {
        return switch (token.type()) {
            case BITWISE -> Ast.UnaryOp.Complement;
            case NEG -> Ast.UnaryOp.Negate;
            case NOT -> Ast.UnaryOp.Not;
            default -> throw new UnsupportedOperationException(token.toString());
        };
    }

    private Token expect(TokenType type) {
        return this.expect(type, NoneParams.NONE);
    }

    /**
     * @param type
     * @param tokenValue null for any
     * @return
     */
    private Token expect(TokenType type, TokenParams tokenValue) {
        String paramStr
                = tokenValue == null ? "any value" : (Objects.equals(NoneParams.NONE, tokenValue) ? "" : tokenValue.toString());
        String msg = paramStr.isEmpty() ? "" : " with value " + paramStr;
        if (pos >= len) {
            throw new ParseException(this, "Expect %s%s,but get %s", type.name(), msg, "EOF");
        }
        Token token = moveToNextToken();
        if (token == null) {
            throw new ParseException(this, "Expect %s%s,but get null", type.name(), msg);
        }
        if (token.type() == type && (tokenValue == null || Objects.equals(tokenValue, token.params()))) {
            return token;
        }
        throw new ParseException(this, "Expect %s%s,but get %s", type.name(), msg, token.toString());
    }

    String reportCurrentPos() {
        int end = Math.min(tokenList.size(), pos + 5);
        var list = tokenList.subList(pos, end);
        String tokens = list.stream().map(i -> i.toString()).collect(Collectors.joining(","));
        return String.format("@Line[%s] near tokens:[%s]", currentLine.params().toString(), tokens);
    }

    /**
     * move pointer pos to nextToken
     *
     * @return nextToken
     */
    private Token moveToNextToken() {
        pos++;
        while (true) {
            if (pos >= tokenList.size()) {
                throw new IllegalStateException();
            }
            Token token = tokenList.get(pos);
            if (token instanceof Tokens.NewLine) {
                this.currentLine = token;
                pos++;
            } else {
                return token;
            }
        }
    }

    private Token getNextToken() {
        Token t;
        int i = pos;
        i++;
        while ((t = tokenList.get(i)) instanceof Tokens.NewLine) {
            i++;
        }
        return t;
    }

    /**
     * 跳过当前token
     */
    private void moveNext() {
        moveToNextToken();
    }

}
