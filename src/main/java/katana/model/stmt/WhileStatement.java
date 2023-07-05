package katana.model.stmt;

import katana.model.expr.Expression;

public class WhileStatement extends Statement {

    public final Expression condition;
    public final Statement body;

    public WhileStatement(Expression condition, Statement body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }
}
