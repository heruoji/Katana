package katana.model.expr;

import katana.model.token.Token;

public class SuperExpression extends Expression {

    public final Token keyword;
    public final Token method;

    public SuperExpression(Token keyword, Token method) {
        this.keyword = keyword;
        this.method = method;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitSuperExpr(this);
    }
}
