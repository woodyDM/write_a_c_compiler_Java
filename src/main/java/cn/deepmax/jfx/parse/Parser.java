package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.lexer.*;

import java.util.List;
import java.util.Objects;

/**
 * 主要是 recursive dscent parsing 解析
 * 中间有部分是： precedence climbing
 * 还有个技术是 pratt parsing ，听说和上面的是等价
 */
public class Parser {
    public final List<Token> tokenList;
    private int pos = 0;
    private final int len;

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

    public Ast.FunctionDefinition parseFunctionDefinition() {

        expect(TokenType.KEYWORD, new StringTokenParam("int"));
        Token idToken = expect(TokenType.ID, null);
        String idName = idToken.params().toString();

        expect(TokenType.OPEN_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.KEYWORD, new StringTokenParam("void"));
        expect(TokenType.CLOSE_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.OPEN_BRACE, NoneParams.NONE);
        AstNode.Statement stmt = parseStatement();
        expect(TokenType.CLOSE_BRACE, NoneParams.NONE);

        return new Ast.FunctionDefinition(idName, stmt);
    }

    public AstNode.Statement parseStatement() {
        expect(TokenType.KEYWORD, new StringTokenParam("return"));
        AstNode.Exp node = parseExp(0);
        Ast.ReturnStatement statement = new Ast.ReturnStatement(node);
        expect(TokenType.SEMICOLON, NoneParams.NONE);
        return statement;
    }

    public AstNode.Factor parseFactor() {
        Token token = moveToNextToken();
        return switch (token.type()) {
            case CONSTANT -> {
                String v = token.params().toString();
                yield new Ast.IntConstantFactor(Integer.parseInt(v));
            }
            case BITWISE, NEG -> {
                AstNode.UnaryOperator op = parseOp(token);
                var innerFact = parseFactor();
                yield new Ast.Unary(op, innerFact);
            }
            case OPEN_PARENTHESIS -> {
                AstNode.Exp inner = parseExp(0);
                expect(TokenType.CLOSE_PARENTHESIS, NoneParams.NONE);
                yield new Ast.ExpFactor(inner);
            }
            default -> throw new UnsupportedOperationException("Malformed factor:" + token.toString());
        };
    }

    public AstNode.Exp parseExp(int minPrec) {
        AstNode.Exp left = new Ast.FactorExp(parseFactor());
        Token nextToken = getNextToken();
        while (nextToken.type().isBinaryOp() && nextToken.type().prec() >= minPrec) {
            moveNext();
            var op = parseBinop(nextToken);

            AstNode.Exp right = parseExp(nextToken.type().prec() + 1);
            left = new Ast.Binary(op, left, right);
            nextToken = getNextToken();
        }
        return left;
    }

    private AstNode.BinaryOperator parseBinop(Token token) {
        return switch (token) {
            case TokenType.PLUS -> Ast.BinaryOp.Add;
            case TokenType.NEG -> Ast.BinaryOp.Subtract;
            case TokenType.MULTIP -> Ast.BinaryOp.Multiply;
            case TokenType.DIV -> Ast.BinaryOp.Divide;
            case TokenType.REMINDER -> Ast.BinaryOp.Remainder;
            default -> throw new UnsupportedOperationException("invalid token " + token.toString());
        };
    }


    private AstNode.UnaryOperator parseOp(Token token) {
        return switch (token.type()) {
            case BITWISE -> Ast.UnaryOp.Complement;
            case NEG -> Ast.UnaryOp.Not;
            default -> throw new UnsupportedOperationException(token.toString());
        };
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
            throw new ParseException("Expect %s%s,but get %s\n", type.name(), msg, "EOF");
        }
        Token token = tokenList.get(pos++);
        if (token == null) {
            throw new ParseException("Expect %s%s,but get null\n", type.name(), msg);
        }
        if (token.type() == type && (tokenValue == null || Objects.equals(tokenValue, token.params()))) {
            return token;
        }
        throw new ParseException("Expect %s%s,but get %s\n", type.name(), msg, token.toString());
    }

    private Token moveToNextToken() {
        var t = getNextToken();
        moveNext();
        return t;
    }

    private Token getNextToken() {
        return tokenList.get(pos);
    }

    private void moveNext() {
        pos++;
    }

}
