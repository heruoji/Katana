package katana.model.expr;

public class GroupingExpression extends Expression {
    public final Expression expression;

    public GroupingExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitGroupingExpr(this);
    }
}
