package katana.model.expr;

import katana.model.token.Token;

public class UnaryExpression extends Expression{
    public final Token operator;
    public final Expression right;

    public UnaryExpression(Token operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}
