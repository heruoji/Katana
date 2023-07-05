package katana.model.expr;

import katana.model.token.Token;

import java.util.List;

public class CallExpression extends Expression {
    public final Expression callee;
    public final Token paren;
    public final List<Expression> arguments;

    public CallExpression(Expression callee, Token paren, List<Expression> arguments) {
        this.callee = callee;
        this.paren = paren;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }
}
