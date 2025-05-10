package cn.deepmax.jfx;

import cn.deepmax.jfx.asm.AsmAst;
import cn.deepmax.jfx.asm.AssemblyConstruct;
import cn.deepmax.jfx.emit.Emission;
import cn.deepmax.jfx.ir.IR;
import cn.deepmax.jfx.ir.IRConverter;
import cn.deepmax.jfx.lexer.Lexer;
import cn.deepmax.jfx.parse.Ast;
import cn.deepmax.jfx.parse.Parser;
import cn.deepmax.jfx.utils.ProcessRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
    static String input =
            """
                   int   main   \t\t\r  \n
                    (  void ) {
                        return ~2+3;    \t\t
                        \n
                    }
                    """;


    public static void main(String[] args) throws IOException {
        String v = System.getProperty("test");
        if (v == null || v.isBlank()) {
            mainLocal(args);
            return;
        }

        runTests(args);
    }

    private static void runTests(String[] args) {
        //for book's tests
        String path = args.length > 1 ? args[1] : args[0];
        String param = args.length > 1 ? args[0] : "";
        try {
            String inputSource = Files.readString(Paths.get(path));
            runTest(param, inputSource, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runTest(String level, String source, String sourcePath) throws IOException {
        Lexer lexer = new Lexer(source);
        Parser p = new Parser(lexer);
        if ("--lex".equals(level)) return;

        Ast.AstProgram astProgram = p.parseProgram();
        if ("--parse".equals(level)) return;

        IRConverter irConverter = new IRConverter(astProgram);
        IR.Program irProgram = irConverter.convertToIR();
        if ("--tacky".equals(level)) return;


        AssemblyConstruct.Program asmAst = AsmAst.createAsmAst(irProgram);
        if ("--codegen".equals(level)) return;
        String asmCode = Emission.codegen(asmAst);

        compileToBinary(sourcePath, asmCode);
    }

    private static void compileToBinary(String sourcePath, String asmCode) throws IOException {
        int i = sourcePath.lastIndexOf(".");
        if (i == -1) throw new RuntimeException("source no suffix " + sourcePath);
        String binPath = sourcePath.substring(0, i);
        String asmPath = sourcePath.substring(0, i) + ".s";


        Path path = Paths.get(asmPath);
        Files.createDirectories(path.getParent());

        Files.writeString(path, asmCode,
                StandardCharsets.UTF_8);

        ProcessRunner.run("gcc", asmPath, "-o", binPath);

    }

    public static void mainLocal(String[] args) throws IOException {
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);
        System.out.println("--------- lexer ----------");
        System.out.println(p.tokenList);

        Ast.AstProgram ast = p.parseProgram();
        System.out.println("--------- parser ---------");
        System.out.println(ast.toString());


        IRConverter irConverter = new IRConverter(ast);
        IR.Program irProgram = irConverter.convertToIR();
        System.out.println("--------- ir ---------");
        System.out.println(irProgram.toString());

        AssemblyConstruct.Program asmAst = AsmAst.createAsmAst(irProgram);
        System.out.println("--------- asm ast ---------");
        System.out.println(asmAst);

        String asmCode = Emission.codegen(asmAst);
        System.out.println("---- asm -------");
        System.out.println(asmCode);


        String asmPath = "out.asm";
        Files.writeString(Paths.get(asmPath), asmCode,
                StandardCharsets.UTF_8);
    }
}
