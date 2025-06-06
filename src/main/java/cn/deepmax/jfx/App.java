package cn.deepmax.jfx;

import cn.deepmax.jfx.asm.AsmAst;
import cn.deepmax.jfx.asm.AssemblyConstruct;
import cn.deepmax.jfx.emit.Emission;
import cn.deepmax.jfx.ir.IR;
import cn.deepmax.jfx.ir.IRConverter;
import cn.deepmax.jfx.lexer.Lexer;
import cn.deepmax.jfx.parse.Ast;
import cn.deepmax.jfx.parse.Parser;
import cn.deepmax.jfx.parse.TypeChecker;
import cn.deepmax.jfx.utils.ProcessRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
    static String input =
            """
                    
                    int putchar(int c);
                    
                    int incr_and_print(int b) {
                        return putchar(b + 2);
                    }
                    
                    int main(void) {
                        incr_and_print(70);
                        return 0;
                    }
                    
                    
                    
                    """;
//    static String input =
//            """
//                    int multiply_many_args(int a, int b, int c, int d, int e, int f, int g, int h){
//                        return a+b+c+d+e+f+g+h;
//                    }
//
//                    int main(void) {
//                        int x = 1;
//
//                        int seven = 7;
//                        int eight = 8;
//                        int y = multiply_many_args(x, 2, 3, 4*4, 5/5, 6, seven,eight);
//                        return y-400;
//                    }
//
//                    """;


    public static void main(String[] args) throws IOException {
        String v = System.getProperty("test");
        if (v == null || v.isBlank()) {
            mainLocal(args);
            return;
        }
//        throw new RuntimeException("Args :" + Arrays.toString(args));
        runTests(args);
    }

    private static void runTests(String[] args) {
        //for book's tests
        Args ag = new Args(args);
        try {
            String inputSource = Files.readString(Paths.get(ag.path));
            runTest(ag, ag.param, inputSource, ag.path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class Args {
        boolean toC;
        String path;
        String param;

        Args(String[] args) {
            int i = 0;
            if (args[0].equals("-c")) {
                this.toC = true;
                i++;
            }
            this.path = args.length > 1 + i ? args[1 + i] : args[i];
            this.param = args.length > 1 + i ? args[i] : "";
        }
    }

    private static void runTest(Args ag, String level, String source, String sourcePath) throws IOException {
        Lexer lexer = new Lexer(source);
        Parser p = new Parser(lexer);
        if ("--lex".equals(level)) return;

        Ast.AstProgram astProgram = p.parseProgram();
        if ("--parse".equals(level)) return;

        astProgram = p.resolver.resolveProgram(astProgram);
        TypeChecker typeChecker = new TypeChecker();
        typeChecker.checkProgram(astProgram);
        if ("--validate".equals(level)) return;

        IRConverter irConverter = new IRConverter(astProgram, typeChecker);
        IR.Program irProgram = irConverter.convertToIR();
        if ("--tacky".equals(level)) return;

        AssemblyConstruct.Program asmAst = AsmAst.createAsmAst(irProgram, typeChecker);
        if ("--codegen".equals(level)) return;
        String asmCode = Emission.codegen(asmAst);


        compileToBinary(ag, sourcePath, asmCode);
    }

    private static void compileToBinary(Args ag, String sourcePath, String asmCode) throws IOException {
        int i = sourcePath.lastIndexOf(".");
        if (i == -1) throw new RuntimeException("source no suffix " + sourcePath);
        String asmPath = sourcePath.substring(0, i) + ".s";
        Path path = Paths.get(asmPath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, asmCode,
                StandardCharsets.UTF_8);
        //compile using gcc
        String binPath = sourcePath.substring(0, i);
        if (ag.toC) {
            String objPath = binPath + ".o";
            ProcessRunner.run("gcc", "-c", asmPath, "-o", objPath);
        } else {
            ProcessRunner.run("gcc", asmPath, "-o", binPath);
        }

    }

    public static void mainLocal(String[] args) throws IOException {
        Lexer lexer = new Lexer(input);
        Parser p = new Parser(lexer);
        System.out.println("--------- lexer ----------");
        System.out.println(p.tokenList);

        Ast.AstProgram ast = p.parseProgram();
        System.out.println("--------- parser ---------");
        System.out.println(ast.toString());
        System.out.println("--------- resovle(validate) ---------");
        ast = p.resolver.resolveProgram(ast);
        TypeChecker typeChecker = new TypeChecker();
        typeChecker.checkProgram(ast);
        System.out.println(ast.toString());


        IRConverter irConverter = new IRConverter(ast, typeChecker);
        IR.Program irProgram = irConverter.convertToIR();
        System.out.println("--------- ir ---------");
        System.out.println(irProgram.toString());

        AssemblyConstruct.Program asmAst = AsmAst.createAsmAst(irProgram, typeChecker);
        System.out.println("--------- asm ast ---------");
        System.out.println(asmAst);
        System.out.printf("-----current variable   -------\n");

        String asmCode = Emission.codegen(asmAst);
        System.out.println("---- asm -------");
        System.out.println(asmCode);


        String asmPath = "out.asm";
        Files.writeString(Paths.get(asmPath), asmCode,
                StandardCharsets.UTF_8);
    }
}
