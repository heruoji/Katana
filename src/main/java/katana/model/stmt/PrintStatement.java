package katana.model.stmt;

import katana.model.expr.Expression;

public class PrintStatement extends Statement {

    public final Expression expression;

    public PrintStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visitPrintStmt(this);
    }
}
