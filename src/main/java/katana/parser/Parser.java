package katana.parser;

import katana.Katana;
import katana.model.expr.*;
import katana.model.stmt.*;
import katana.model.token.Token;
import katana.model.token.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static katana.model.token.TokenType.*;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseDeclaration());
        }

        return statements;
    }

    private Statement parseDeclaration() {
        try {
            if (match(CLASS)) {
                return parseClassDeclaration();
            }
            if (match(FUN)) {
                return parseFunctionDeclaration();
            }
            if (match(VAR)) {
                return parseVarDeclaration();
            }
            return parseStatement();

        } catch (ParseError error) {
            skipToNextStatement();
            return null;
        }
    }

    private Statement parseClassDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        VariableExpression superClass = parseSuperClass();
        List<FunctionStatement> methods = parseMethods();

        return new ClassStatement(name, superClass, methods);
    }

    private VariableExpression parseSuperClass() {
        VariableExpression superClass = null;
        if (match(EXTENDS)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superClass = new VariableExpression(previous());
        }
        return superClass;
    }

    private List<FunctionStatement> parseMethods() {
        consume(LEFT_BRACE, "Expect '{' before class body.");
        List<FunctionStatement> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(parseFunctionDeclaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after class body");
        return methods;
    }

    private FunctionStatement parseFunctionDeclaration() {
        Token name = consume(IDENTIFIER, "Expect function name.");
        List<Token> parameters = parseFunctionParameters();
        consume(LEFT_BRACE, "Expect '{' before function body.");
        List<Statement> body = parseBlock();
        return new FunctionStatement(name, parameters, body);
    }

    private List<Token> parseFunctionParameters() {
        consume(LEFT_PAREN, "Expect '(' after function name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                checkParameters(parameters);
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        return parameters;
    }

    private List<Statement> parseBlock() {
        List<Statement> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(parseDeclaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Statement parseVarDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expression initializer = parseInitializer();
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new VarStatement(name, initializer);
    }

    private Expression parseInitializer() {
        Expression initializer = null;
        if (match(EQUAL)) {
            initializer = parseExpression();
        }
        return initializer;
    }

    private Statement parseStatement() {
        if (match(FOR)) {
            return parseForStatement();
        }
        if (match(IF)) {
            return parseIfStatement();
        }
        if (match(PRINT)) {
            return parsePrintStatement();
        }
        if (match(RETURN)) {
            return parseReturnStatement();
        }
        if (match(WHILE)) {
            return whileStatement();
        }
        if (match(LEFT_BRACE)) {
            return parseBlockStatement();
        }
        return parseExpressionStatement();
    }

    private Statement parseForStatement() {

        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Statement initializer = parseForInitializer();

        Expression condition = parseForCondition();

        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expression increment = parseForIncrement();

        consume(RIGHT_PAREN, "Expect ')' after 'for' clauses.");

        return convertToWhileStatement(initializer, condition, increment);

    }

    private Statement parseForInitializer() {
        Statement initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = parseVarDeclaration();
        } else {
            initializer = parseExpressionStatement();
        }
        return initializer;
    }

    private Expression parseForCondition() {
        Expression condition = null;
        if (!check(SEMICOLON)) {
            condition = parseExpression();
        }
        return condition;
    }

    private Expression parseForIncrement() {
        Expression increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = parseExpression();
        }
        return increment;
    }

    private Statement convertToWhileStatement(Statement initializer, Expression condition, Expression increment) {

        Statement body = parseStatement();

        if (increment != null) {
            body = new BlockStatement(Arrays.asList(body, new ExpressionStatement(increment)));
        }

        if (condition == null) {
            condition = new LiteralExpression(true);
        }
        body = new WhileStatement(condition, body);

        if (initializer != null) {
            body = new BlockStatement(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Statement parseBlockStatement() {
        return new BlockStatement(parseBlock());
    }


    private Statement parseIfStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = parseExpression();
        consume(RIGHT_PAREN, "Expect ')' after 'if' condition.");

        Statement thenBranch = parseStatement();
        Statement elseBranch = null;
        if (match(ELSE)) {
            elseBranch = parseStatement();
        }

        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private Statement parsePrintStatement() {
        Expression value = parseExpression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new PrintStatement(value);
    }

    private Statement parseReturnStatement() {
        Token keyword = previous();
        Expression value = null;
        if (!check(SEMICOLON)) {
            value = parseExpression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new ReturnStatement(keyword, value);
    }

    private Statement whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = parseExpression();
        consume(RIGHT_PAREN, "Expect ')' after 'while' condition.");
        Statement body = parseStatement();

        return new WhileStatement(condition, body);
    }

    private Statement parseExpressionStatement() {
        Expression expression = parseExpression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new ExpressionStatement(expression);
    }


    //    Parse Expressions

    private Expression parseExpression() {
        return parseAssignment();
    }

    private Expression parseAssignment() {
        Expression expression = parseOr();

        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = parseAssignment();

            if (expression instanceof VariableExpression) {
                Token name = ((VariableExpression) expression).name;
                return new AssignExpression(name, value);
            } else if (expression instanceof GetExpression getExpression) {
                return new SetExpression(getExpression.object, getExpression.name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    private Expression parseOr() {
        Expression expression = parseAnd();

        while (match(OR)) {
            Token operator = previous();
            Expression right = parseAnd();
            expression = new LogicalExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseAnd() {
        Expression expression = parseEquality();

        while (match(AND)) {
            Token operator = previous();
            Expression right = parseEquality();
            expression = new LogicalExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseEquality() {
        Expression expression = parseComparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = parseComparison();
            expression = new BinaryExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseComparison() {
        Expression expression = parseTerm();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = parseTerm();
            expression = new BinaryExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseTerm() {
        Expression expression = parseFactor();
        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expression right = parseFactor();
            expression = new BinaryExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseFactor() {
        Expression expression = parseUnary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression right = parseUnary();
            expression = new BinaryExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseUnary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expression right = parseUnary();
            return new UnaryExpression(operator, right);
        }

        return parseCall();
    }

    private Expression parseCall() {
        Expression expression = parsePrimary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expression = parseFinishCall(expression);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expression = new GetExpression(expression, name);
            } else {
                break;
            }
        }

        return expression;
    }

    private Expression parseFinishCall(Expression callee) {
        List<Expression> arguments = parseFinishCallArguments();
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new CallExpression(callee, paren, arguments);
    }

    private List<Expression> parseFinishCallArguments() {
        List<Expression> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                checkArguments(arguments);
                arguments.add(parseExpression());
            } while (match(COMMA));
        }
        return arguments;
    }

    private Expression parsePrimary() {
        if (match(FALSE)) {
            return new LiteralExpression(false);
        }
        if (match(TRUE)) {
            return new LiteralExpression(true);
        }
        if (match(NULL)) {
            return new LiteralExpression(null);
        }
        if (match(NUMBER, STRING)) {
            return new LiteralExpression(previous().javaLiteral);
        }

        if (match(SUPER)) {
            return parseSuperExpression();
        }

        if (match(THIS)) {
            return new ThisExpression(previous());
        }

        if (match(IDENTIFIER)) {
            return new VariableExpression(previous());
        }

        if (match(LEFT_PAREN)) {
            return parseGroupingExpression();
        }

        throw error(peek(), "Expect expression.");
    }

    private Expression parseSuperExpression() {
        Token keyword = previous();
        consume(DOT, "Expect '.' after 'super'.");
        Token method = consume(IDENTIFIER, "Expect superclass method name after 'super.'.");
        return new SuperExpression(keyword, method);
    }

    private Expression parseGroupingExpression() {
        Expression expression = parseExpression();
        consume(RIGHT_PAREN, "Expect ')' after expression.");
        return new GroupingExpression(expression);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            advance();
            return previous();
        }

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    private void advance() {
        if (!isAtEnd()) {
            current++;
        }
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Katana.error(token, message);
        return new ParseError();
    }

    private void checkParameters(List<Token> parameters) {
        if (!validateParameterSize(parameters.size())) {
            throw error(peek(), "Can't have more than 255 parameters.");
        }
    }

    private boolean validateParameterSize(int parameterSize) {
        return parameterSize < 255;
    }

    private void checkArguments(List<Expression> arguments) {
        if (!validateArgumentsSize(arguments.size())) {
            throw error(peek(), "Can't have more than 255 arguments.");
        }
    }

    private boolean validateArgumentsSize(int argumentsSize) {
        return argumentsSize < 255;
    }

    private void skipToNextStatement() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS, FUN, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
            }

            advance();
        }
    }


}
