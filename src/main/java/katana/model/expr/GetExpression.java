package katana.model.expr;

import katana.model.token.Token;

public class GetExpression extends Expression {
    public final Expression object;
    public final Token name;

    public GetExpression(Expression object, Token name) {
        this.object = object;
        this.name = name;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitGetExpr(this);
    }
}
