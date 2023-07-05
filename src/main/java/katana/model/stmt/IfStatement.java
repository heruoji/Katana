package katana.model.stmt;

import katana.model.expr.Expression;

public class IfStatement extends Statement {

    public final Expression condition;
    public final Statement thenBranch;
    public final Statement elseBranch;

    public IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
}
