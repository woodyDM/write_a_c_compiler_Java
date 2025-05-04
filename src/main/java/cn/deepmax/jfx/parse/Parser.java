package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.lexer.*;

import java.util.Objects;

public class Parser {

    public final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public AstNode parseProgram() {
        AST.Program p = new AST.Program();
        p.add(parseFunctionDefinition());
        Token left = lexer.nextToken();
        if (left != null) {
            throw new ParseException("Expect end ,but steal has token %s\n", left.toString());
        }
        return p;
    }

    public AstNode parseFunctionDefinition() {
        AST.FunctionDefinition fn = new AST.FunctionDefinition();
        expect(TokenType.KEYWORD, new StringTokenParam("int"));
        Token idToken = expect(TokenType.ID, null);
        AST.Identifier idNode = new AST.Identifier((Tokens.Id) idToken);
        fn.add(idNode);

        expect(TokenType.OPEN_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.KEYWORD, new StringTokenParam("void"));
        expect(TokenType.CLOSE_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.OPEN_BRACE, NoneParams.NONE);
        AstNode stmt = parseStatement();
        expect(TokenType.CLOSE_BRACE, NoneParams.NONE);
        fn.add(stmt);

        return fn;

    }

    public AstNode parseStatement() {
        expect(TokenType.KEYWORD, new StringTokenParam("return"));
        AstNode node = parseExp();
        AST.Statement statement = new AST.Statement();
        statement.add(node);
        expect(TokenType.SEMICOLON, NoneParams.NONE);
        return statement;
    }

    public AstNode parseExp() {
        Token intToken = expect(TokenType.CONSTANT, null);
        return new AST.IntExp((Tokens.Constant) intToken);
    }

    /**
     * @param type
     * @param tokenValue null for any
     * @return
     */
    private Token expect(TokenType type, TokenParams tokenValue) {
        Token token = lexer.nextToken();
        String paramStr = tokenValue == null ? "" : tokenValue.toString();
        if (token == null) {
            throw new ParseException("Expect %s with value %s,but get null\n", type.name(), paramStr);
        }
        if (token.type() == type && (tokenValue == null || Objects.equals(tokenValue, token.params()))) {
            return token;
        }
        throw new ParseException("Expect %s with value %s,but get %s\n", type.name(), paramStr, token.toString());
    }

}
