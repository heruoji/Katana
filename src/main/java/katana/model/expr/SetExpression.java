package katana.model.expr;

import katana.model.token.Token;

public class SetExpression extends Expression {
    public final Expression object;
    public final Token name;
    public final Expression value;

    public SetExpression(Expression object, Token name, Expression value) {
        this.object = object;
        this.name = name;
        this.value = value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitSetExpr(this);
    }
}
