package katana.model.stmt;

import java.util.List;

public class BlockStatement extends Statement {
    public final List<Statement> statements;

    public BlockStatement(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visitBlockStmt(this);
    }
}
