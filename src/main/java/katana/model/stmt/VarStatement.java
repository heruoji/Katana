package katana.model.stmt;

import katana.model.expr.Expression;
import katana.model.token.Token;

public class VarStatement extends Statement {

    public final Token name;
    public final Expression initializer;

    public VarStatement(Token name, Expression initializer) {
        this.name = name;
        this.initializer = initializer;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visitVarStmt(this);
    }
}
