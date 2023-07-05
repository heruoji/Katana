package katana.model.expr;

public abstract class Expression {
    public abstract <T> T accept(ExprVisitor<T> visitor);
}
