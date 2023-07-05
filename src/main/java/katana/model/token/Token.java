package katana.model.token;

public class Token {
    public TokenType type;
    public String rawText;
    public Object javaLiteral;
    public int line;
    public int position;

    public Token(TokenType type, String rawText, Object javaLiteral, int line, int position) {
        this.type = type;
        this.rawText = rawText;
        this.javaLiteral = javaLiteral;
        this.line = line;
        this.position = position;
    }

    public String toString() {
        return type + " " + rawText + " " + javaLiteral;
    }
}
