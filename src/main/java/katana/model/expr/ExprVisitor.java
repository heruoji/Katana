package katana.model.expr;

public interface ExprVisitor<T> {
    T visitAssignExpr(AssignExpression expr);
    T visitBinaryExpr(BinaryExpression expr);
    T visitCallExpr(CallExpression expr);
    T visitGetExpr(GetExpression expr);
    T visitGroupingExpr(GroupingExpression expr);
    T visitLiteralExpr(LiteralExpression expr);
    T visitLogicalExpr(LogicalExpression expr);
    T visitSetExpr(SetExpression expr);
    T visitSuperExpr(SuperExpression expr);
    T visitThisExpr(ThisExpression expr);
    T visitUnaryExpr(UnaryExpression expr);
    T visitVariableExpr(VariableExpression expr);
}
