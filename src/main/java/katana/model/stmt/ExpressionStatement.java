package katana.model.stmt;

import katana.model.expr.Expression;

public class ExpressionStatement extends Statement {

    public final Expression expression;

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visitExpressionStmt(this);
    }
}
