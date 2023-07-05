package katana.model.expr;

import katana.model.token.Token;

public class ThisExpression extends Expression {

    public final Token keyword;

    public ThisExpression(Token keyword) {
        this.keyword = keyword;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitThisExpr(this);
    }
}
