package katana.model.stmt;

public interface StmtVisitor<T> {
    T visitBlockStmt(BlockStatement stmt);
    T visitClassStmt(ClassStatement stmt);
    T visitExpressionStmt(ExpressionStatement stmt);
    T visitFunctionStmt(FunctionStatement stmt);
    T visitIfStmt(IfStatement stmt);
    T visitPrintStmt(PrintStatement stmt);
    T visitReturnStmt(ReturnStatement stmt);
    T visitVarStmt(VarStatement stmt);
    T visitWhileStmt(WhileStatement stmt);
}
