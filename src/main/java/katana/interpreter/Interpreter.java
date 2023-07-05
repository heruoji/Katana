package katana.interpreter;

import katana.Katana;
import katana.interpreter.library.Clock;
import katana.model.expr.*;
import katana.model.stmt.*;
import katana.model.token.Token;
import katana.model.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();

    public Interpreter() {
        setLibraries();
    }
    private void setLibraries() {
        globals.define("clock", new Clock());
    }

    public void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Katana.runtimeError(error);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    public void resolve(Expression expression, int depth) {
        locals.put(expression, depth);
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignExpr(AssignExpression expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpression expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left <= (double) right;
            }
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case MINUS -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case PLUS -> {
                if (isNumbers(left, right)) {
                    return (double) left + (double) right;
                }
                if (isStrings(left, right)) {
                    return left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operand must be two numbers or two strings.");
            }
            case SLASH -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperand(expr.operator, left, right);
                return (double) left * (double) right;
            }
        }

        return null;
    }

    @Override
    public Object visitCallExpr(CallExpression expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expression argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof KatanaCallable function)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(GetExpression expr) {
        Object object = evaluate(expr.object);
        if (object instanceof KatanaInstance) {
            return ((KatanaInstance) object).get(expr.name);
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitGroupingExpr(GroupingExpression expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(LiteralExpression expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(LogicalExpression expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            } else {
                if (!isTruthy(left)) {
                    return left;
                }
            }
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(SetExpression expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof KatanaInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((KatanaInstance) object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(SuperExpression expr) {
        int distance = locals.get(expr);
        KatanaClass superClass = (KatanaClass) environment.getAt(distance, "super");
        KatanaInstance object = (KatanaInstance) environment.getAt(distance - 1, "this");
        KatanaFunction method = superClass.findMethod(expr.method.rawText);

        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.rawText + "'.");
        }

        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(ThisExpression expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(UnaryExpression expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            }
            case BANG -> {
                return !isTruthy(right);
            }
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(VariableExpression expr) {
        return lookUpVariable(expr.name, expr);
    }

    @Override
    public Void visitBlockStmt(BlockStatement stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(ClassStatement stmt) {
        KatanaClass superClass = evaluateSuperClass(stmt);

        environment.define(stmt.name.rawText, null);

        if (superClass != null) {
            environment = new Environment(environment);
            environment.define("super", superClass);
        }

        Map<String, KatanaFunction> methods = new HashMap<>();
        for (FunctionStatement method : stmt.methods) {
            KatanaFunction function = new KatanaFunction(method, environment, method.name.rawText.equals("constructor"));
            methods.put(method.name.rawText, function);
        }

        KatanaClass klass = new KatanaClass(stmt.name.rawText, superClass, methods);

        if (superClass != null) {
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, klass);
        return null;
    }

    private KatanaClass evaluateSuperClass(ClassStatement stmt) {
        if (stmt.superClass != null) {
            Object superClass = evaluate(stmt.superClass);
            if (!(superClass instanceof KatanaClass)) {
                throw new RuntimeError(stmt.superClass.name, "Superclass must be a class.");
            }
            return (KatanaClass) superClass;
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(ExpressionStatement stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(FunctionStatement stmt) {
        KatanaFunction function = new KatanaFunction(stmt, environment, false);
        environment.define(stmt.name.rawText, function);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStatement stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStatement stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStatement stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }

        throw new ReturnValue(value);
    }

    @Override
    public Void visitVarStmt(VarStatement stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.rawText, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStatement stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private String stringify(Object object) {
        if (object == null) {
            return "null";
        }

        if (isNumber(object)) {
            return doubleStringify((Double) object);
        }

        return object.toString();
    }

    private String doubleStringify(Double obj) {
        String text = obj.toString();
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (isNumber(operand)) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (isNumbers(left, right)) {
            return;
        }
        throw new RuntimeError(operator, "Operands must be numbers");
    }

    private boolean isNumber(Object obj) {
        return obj instanceof Double;
    }

    private boolean isNumbers(Object left, Object right) {
        return left instanceof Double && right instanceof Double;
    }

    private boolean isStrings(Object left, Object right) {
        return left instanceof String && right instanceof String;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }

        return a.equals(b);
    }

    private Object lookUpVariable(Token name, Expression expression) {
        Integer distance = locals.get(expression);
        if (distance != null) {
            return environment.getAt(distance, name.rawText);
        } else {
            return globals.get(name);
        }
    }
}
