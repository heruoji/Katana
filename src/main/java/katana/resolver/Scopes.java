package katana.resolver;

import katana.Katana;
import katana.model.token.Token;

import java.util.Stack;

public class Scopes {
    private final Stack<Scope> scopes = new Stack<>();

    public Scope peek() {
        return this.scopes.peek();
    }

    public void beginScope() {
        this.scopes.push(new Scope());
    }

    public void endScope() {
        this.scopes.pop();
    }

    public boolean isEmpty() {
        return this.scopes.isEmpty();
    }

    public void declare(Token name) {
        if (scopes.isEmpty()) return;

        Scope scope = scopes.peek();
        if (scope.declaredVariable(name.rawText)) {
            Katana.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.rawText, false);
    }

    public void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        scopes.peek().put(name.rawText, true);
    }

    public int size() {
        return this.scopes.size();
    }

    public Scope get(int i) {
        return this.scopes.get(i);
    }
}
