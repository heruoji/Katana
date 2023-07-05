package katana;

import katana.interpreter.Interpreter;
import katana.interpreter.RuntimeError;
import katana.model.stmt.Statement;
import katana.model.token.Token;
import katana.model.token.TokenType;
import katana.parser.Parser;
import katana.resolver.Resolver;
import katana.scanner.Scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Katana {

    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: katana [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while(true){
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            run(line);
        }
    }

    private static void run(String source) {

        List<Token> tokens = scanTokens(source);

        List<Statement> statements = parseStatement(tokens);

        if (hadError) return;

        resolveStatement(statements);

        if (hadError) return;

        interpret(statements);
    }

    private static List<Token> scanTokens(String source) {
        Scanner scanner = new Scanner(source);
        return scanner.scanTokens();
    }

    private static List<Statement> parseStatement(List<Token> tokens) {
        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    private static void resolveStatement(List<Statement> statements) {
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);
    }

    private static void interpret(List<Statement> statements) {
        interpreter.interpret(statements);
    }

    public static void error(int line, int position, String message) {
        reportError(line, position, "", message);
        hadError = true;
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            reportError(token.line, token.position, " at end", message);
        } else {
            reportError(token.line, token.position, " at '" + token.rawText + "'", message);
        }
        hadError = true;
    }

    private static void reportError(int line, int position, String where, String message) {
        System.err.println("[line " + line + ", position " + position + "] ERROR" + where + ": " + message);
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.getLine() + ", position " + error.getPosition() + "]");
        hadRuntimeError = true;
    }
}
