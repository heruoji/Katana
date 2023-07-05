package katana.model.stmt;

import katana.model.expr.VariableExpression;
import katana.model.token.Token;

import java.util.List;

public class ClassStatement extends Statement {

    public final Token name;
    public final VariableExpression superClass;
    public final List<FunctionStatement> methods;

    public ClassStatement(Token name, VariableExpression superClass, List<FunctionStatement> methods) {
        this.name = name;
        this.superClass = superClass;
        this.methods = methods;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
        return visitor.visitClassStmt(this);
    }
}
