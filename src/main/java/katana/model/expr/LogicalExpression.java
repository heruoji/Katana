package katana.model.expr;

import katana.model.token.Token;

public class LogicalExpression extends Expression{
    public final Expression left;
    public final Token operator;
    public final Expression right;

    public LogicalExpression(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitLogicalExpr(this);
    }
}
