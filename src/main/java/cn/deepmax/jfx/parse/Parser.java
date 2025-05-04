package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.lexer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Parser {


    private final List<Token> tokenList;
    private int pos = 0;
    private final int len;

    public Parser(Lexer lexer) {
        this.tokenList = lexer.tokenList();
        this.len = this.tokenList.size();
    }

    public AST.Program parseProgram() {
        AST.FunctionDefinition fnDef = parseFunctionDefinition();
        AST.Program p = new AST.Program(fnDef);

        expect(TokenType.EOF, NoneParams.NONE);
        return p;
    }

    public AST.FunctionDefinition parseFunctionDefinition() {

        expect(TokenType.KEYWORD, new StringTokenParam("int"));
        Token idToken = expect(TokenType.ID, null);
        String idName = ((Tokens.Id) idToken).params().toString();

        expect(TokenType.OPEN_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.KEYWORD, new StringTokenParam("void"));
        expect(TokenType.CLOSE_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.OPEN_BRACE, NoneParams.NONE);
        AST.Statement stmt = parseStatement();
        expect(TokenType.CLOSE_BRACE, NoneParams.NONE);

        AST.FunctionDefinition fn = new AST.FunctionDefinition(idName, stmt);

        return fn;

    }

    public AST.Statement parseStatement() {
        expect(TokenType.KEYWORD, new StringTokenParam("return"));
        AST.Exp node = parseExp();
        AST.Statement statement = new AST.Statement(node);
        expect(TokenType.SEMICOLON, NoneParams.NONE);
        return statement;
    }

    public AST.Exp parseExp() {
        Token intToken = expect(TokenType.CONSTANT, null);
        String v = ((Tokens.Constant) intToken).params().toString();
        return new AST.Exp(Integer.parseInt(v));
    }

    /**
     * @param type
     * @param tokenValue null for any
     * @return
     */
    private Token expect(TokenType type, TokenParams tokenValue) {
        String paramStr = tokenValue == null ? "" : tokenValue.toString();
        if (pos >= len) {
            throw new ParseException("Expect %s with value %s,but get %s\n", type.name(), paramStr, "EOF");
        }
        Token token = tokenList.get(pos++);
        if (token == null) {
            throw new ParseException("Expect %s with value %s,but get null\n", type.name(), paramStr);
        }
        if (token.type() == type && (tokenValue == null || Objects.equals(tokenValue, token.params()))) {
            return token;
        }
        throw new ParseException("Expect %s with value %s,but get %s\n", type.name(), paramStr, token.toString());
    }

}
