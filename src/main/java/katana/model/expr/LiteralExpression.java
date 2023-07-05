package katana.model.expr;

public class LiteralExpression extends Expression{

    public final Object value;

    public LiteralExpression(Object value) {
        this.value = value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitLiteralExpr(this);
    }
}
