package katana.interpreter;

import katana.model.token.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public int getLine() {
        return token.line;
    }

    public int getPosition() {
        return token.position;
    }
}
