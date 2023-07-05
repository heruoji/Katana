package katana.model.stmt;

import katana.model.expr.Expression;
import katana.model.token.Token;

public class ReturnStatement extends Statement {

    public final Token keyword;
    public final Expression value;

    public ReturnStatement(Token keyword, Expression value) {
        this.keyword = keyword;
        this.value = value;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visitReturnStmt(this);
    }
}
