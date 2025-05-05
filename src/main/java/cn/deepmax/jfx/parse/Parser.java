package cn.deepmax.jfx.parse;

import cn.deepmax.jfx.lexer.*;

import java.util.List;
import java.util.Objects;

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
        String idName = ((Tokens.Id) idToken).params().toString();

        expect(TokenType.OPEN_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.KEYWORD, new StringTokenParam("void"));
        expect(TokenType.CLOSE_PARENTHESIS, NoneParams.NONE);
        expect(TokenType.OPEN_BRACE, NoneParams.NONE);
        Ast.ReturnStatement stmt = parseStatement();
        expect(TokenType.CLOSE_BRACE, NoneParams.NONE);

        Ast.FunctionDefinition fn = new Ast.FunctionDefinition(idName, stmt);

        return fn;

    }

    public Ast.ReturnStatement parseStatement() {
        expect(TokenType.KEYWORD, new StringTokenParam("return"));
        Ast.IntExp node = parseExp();
        Ast.ReturnStatement statement = new Ast.ReturnStatement(node);
        expect(TokenType.SEMICOLON, NoneParams.NONE);
        return statement;
    }

    public Ast.IntExp parseExp() {
        Token intToken = expect(TokenType.CONSTANT, null);
        String v = ((Tokens.Constant) intToken).params().toString();
        return new Ast.IntExp(Integer.parseInt(v));
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

}
