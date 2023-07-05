package katana.resolver;

import katana.Katana;
import katana.interpreter.Interpreter;
import katana.model.expr.*;
import katana.model.stmt.*;
import katana.model.token.Token;

import java.util.List;

public class Resolver implements ExprVisitor<Void>, StmtVisitor<Void> {
    private final Interpreter interpreter;
    private final Scopes scopes = new Scopes();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;


    private enum FunctionType {
        NONE, FUNCTION, INITIALIZER, METHOD
    }

    private enum ClassType {
        NONE, CLASS
    }

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<Statement> statements) {
        for (Statement statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }

    private void resolveFunction(FunctionStatement function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        scopes.beginScope();
        resolveParams(function.params);
        resolve(function.body);
        scopes.endScope();
        currentFunction = enclosingFunction;
    }

    private void resolveParams(List<Token> params) {
        for (Token param : params) {
            scopes.declare(param);
            scopes.define(param);
        }
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).declaredVariable(name.rawText)) {
                interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }

    @Override
    public Void visitAssignExpr(AssignExpression expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpression expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(CallExpression expr) {
        resolve(expr.callee);

        for (Expression argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGetExpr(GetExpression expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(GroupingExpression expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(LiteralExpression expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(LogicalExpression expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(SetExpression expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSuperExpr(SuperExpression expr) {
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitThisExpr(ThisExpression expr) {
        if (currentClass == ClassType.NONE) {
            Katana.error(expr.keyword, "Can't use 'this' outside of a class");
            return null;
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitUnaryExpr(UnaryExpression expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(VariableExpression expr) {
        if (!scopes.isEmpty() && this.scopes.peek().get(expr.name.rawText) == Boolean.FALSE) {
            Katana.error(expr.name, "Can't read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStatement stmt) {
        scopes.beginScope();
        resolve(stmt.statements);
        scopes.endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(ClassStatement stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        scopes.declare(stmt.name);
        scopes.define(stmt.name);

        if (isInheritItself(stmt)) {
            Katana.error(stmt.superClass.name, "A class can't inherit from itself.");
        }

        if (hasSuperClass(stmt)) {
            resolve(stmt.superClass);
        }

        if (hasSuperClass(stmt)) {
            scopes.beginScope();
            scopes.peek().put("super", true);
        }

        scopes.beginScope();
        scopes.peek().put("this", true);

        resolveMethods(stmt);

        scopes.endScope();

        if (hasSuperClass(stmt)) {
            scopes.endScope();
        }

        currentClass = enclosingClass;
        return null;
    }

    private boolean isInheritItself(ClassStatement stmt) {
        return stmt.superClass != null && stmt.name.rawText.equals(stmt.superClass.name.rawText);
    }

    private boolean hasSuperClass(ClassStatement stmt) {
        return stmt.superClass != null;
    }

    private void resolveMethods(ClassStatement stmt) {
        for (FunctionStatement method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.rawText.equals("constructor")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }
    }

    @Override
    public Void visitExpressionStmt(ExpressionStatement stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(FunctionStatement stmt) {
        scopes.declare(stmt.name);
        scopes.define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStatement stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStatement stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStatement stmt) {
        if (currentFunction == FunctionType.NONE) {
            Katana.error(stmt.keyword, "Can't return form top-level code.");
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Katana.error(stmt.keyword, "Can't return a value from an initializer.");
            }

            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(VarStatement stmt) {
        scopes.declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        scopes.define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStatement stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }
}
