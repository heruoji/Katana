package katana.model.stmt;

import katana.model.token.Token;

import java.util.List;

public class FunctionStatement extends Statement {

    public final Token name;
    public final List<Token> params;
    public final List<Statement> body;

    public FunctionStatement(Token name, List<Token> params, List<Statement> body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visitFunctionStmt(this);
    }
}
