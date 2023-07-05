package katana.model.expr;

import katana.model.token.Token;

public class VariableExpression extends Expression {
    public final Token name;

    public VariableExpression(Token name) {
        this.name = name;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitVariableExpr(this);
    }
}
