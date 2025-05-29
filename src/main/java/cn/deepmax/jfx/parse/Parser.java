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

    public final Resolver resolver = new Resolver();
    private Token currentLine;

    public Parser(Lexer lexer) {
        this.tokenList = lexer.tokenList();
        this.len = this.tokenList.size();
    }

    public Ast.AstProgram parseProgram() {
        List<Ast.FunctionDeclare> funcs = parseFunctionDeclarationList();
        Ast.AstProgram p = new Ast.AstProgram(funcs);

        expect(TokenType.EOF, NoneParams.NONE);
        return p;
    }

    public List<Ast.FunctionDeclare> parseFunctionDeclarationList() {
        List<Ast.FunctionDeclare> result = new ArrayList<>();
        Ast.FunctionDeclare it;
        while ((it = parseFunctionDeclaration()) != null) {
            result.add(it);
        }
        return result;
    }

    private Ast.FunctionDeclare parseFunctionDeclaration() {
        if (getNextToken() == TokenType.EOF) {
            return null;
        }
        expect(TokenType.KEYWORD, new StringTokenParam("int"));
        Token idToken = expect(TokenType.ID, null);

        expect(TokenType.OPEN_PARENTHESIS, NoneParams.NONE);
        var paramList = parseParamList();
        expect(TokenType.CLOSE_PARENTHESIS, NoneParams.NONE);
        Token nextToken = getNextToken();

        Ast.Block block = null;
        if (nextToken == TokenType.SEMICOLON) {
            //declare
            moveNext();
        } else {
            //definite
            List<AstNode.BlockItem> fnBody = new ArrayList<>();

            expect(TokenType.OPEN_BRACE, NoneParams.NONE);
            parseFunctionBody(fnBody);
            expect(TokenType.CLOSE_BRACE, NoneParams.NONE);
            block = new Ast.Block(fnBody);
        }
        return new Ast.FunctionDeclare(idToken.params().toString(), paramList, block);
    }

    /**
     * 函数声明参数列表
     *
     * @return
     */
    private List<AstNode.Param> parseParamList() {
        List<AstNode.Param> list = new ArrayList<>();
        Token next;
        while (true) {
            next = getNextToken();
            if (next == TokenType.CLOSE_PARENTHESIS) {
                break;
            }
            if (next == TokenType.COMMA) {
                if (list.isEmpty()) {
                    throw new ParseException(this, "no param before comma!");
                }
                moveNext();
                next = getNextToken();
            }
            if (next.isKeyword("void")) {
                if (list.isEmpty()) {
                    moveNext();
                    return list;
                } else {
                    throw new ParseException(this, "Function not pure void");
                }
            } else if (next.isKeyword("int")) {
                moveNext();
                var nx = getNextToken();
                if (nx.type() == TokenType.ID) {
                    Token id = expect(TokenType.ID, null);
                    list.add(new Ast.VarParam("int", id.params().toString()));
                } else if (nx == TokenType.COMMA || nx == TokenType.CLOSE_PARENTHESIS) {
                    list.add(new Ast.VarParam("int", null));
                } else {
                    throw new ParseException(this, "expect id|comma|) , but got " + nx);
                }
            } else {
                throw new ParseException(this, "expect keyword[void] or keyword[int] ,but got " + next);
            }
        }
        return list;
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
            Token id = expect(TokenType.ID, null);
            var next = getNextToken();
            AstNode.Declaration declaration = next == TokenType.OPEN_PARENTHESIS ?
                    parseFuncDeclaration((Tokens.Id) id) :
                    parseVarDeclaration((Tokens.Id) id, next);

            return new Ast.DeclareBlockItem(declaration);
        }
        AstNode.Statement statement = parseStatement();
        return new Ast.StatementBlockItem(statement);
    }

    private AstNode.Declaration parseFuncDeclaration(Tokens.Id id) {
        moveNext();
        //parse param list
        List<AstNode.Param> params = parseParamList();
        expect(TokenType.CLOSE_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.SEMICOLON);
        return new Ast.FunctionDeclare(id.params().toString(), params, null);
    }

    private AstNode.Declaration parseVarDeclaration(Tokens.Id id, Token next) {
        AstNode.Exp init = null;
        if (next == TokenType.ASSIGNMENT) {
            //init
            moveNext();
            init = parseExp(0);
        }
        expect(TokenType.SEMICOLON);
        return new Ast.VarDeclare(id.params().toString(), init);
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
            Token id = expect(TokenType.ID, null);
            var next = getNextToken();
            var dec = parseVarDeclaration((Tokens.Id) id, next);
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


    public AstNode.Factor parseFactor() {
        Token token = moveToNextToken();
        return switch (token.type()) {
            case ID -> {
                Token nt = getNextToken();
                if (nt == TokenType.OPEN_PARENTHESIS) {
                    //function call
                    expect(TokenType.OPEN_PARENTHESIS);
                    Ast.ArgumentList argumentList = parseArgList();
                    expect(TokenType.CLOSE_PARENTHESIS);
                    yield new Ast.FunctionCall(token.params().toString(), argumentList.expList());
                } else {
                    Tokens.Id id = (Tokens.Id) token;
                    Ast.Var exp = new Ast.Var(id.params().toString());
                    yield new Ast.ExpFactor(exp);
                }
            }
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

            default -> throw new ParseException(this, "Malformed factor:" + token);
        };
    }

    private Ast.ArgumentList parseArgList() {
        Token t;
        List<AstNode.Exp> list = new ArrayList<>();
        while ((t = getNextToken()) != TokenType.CLOSE_PARENTHESIS) {
            if (t == TokenType.COMMA) {
                moveNext();
            }
            AstNode.Exp exp = parseExp(0);
            list.add(exp);
        }
        return new Ast.ArgumentList(list);
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
        return String.format("@Line[%s] near tokens:[%s]", lineInfo(), tokens);
    }

    private String lineInfo() {
        return currentLine == null ? "1" : currentLine.params().toString();
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
