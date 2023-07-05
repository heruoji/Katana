package katana.model.expr;

import katana.model.token.Token;

public class AssignExpression extends Expression {
    public final Token name;
    public final Expression value;

    public AssignExpression(Token token, Expression value) {
        this.name = token;
        this.value = value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitAssignExpr(this);
    }
}
