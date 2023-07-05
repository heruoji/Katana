package katana.model.stmt;

public abstract class Statement {
    public abstract <T> T accept(StmtVisitor<T> visitor);
}
