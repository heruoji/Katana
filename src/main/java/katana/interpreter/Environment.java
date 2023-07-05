package katana.interpreter;

import katana.model.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public Environment() {
        enclosing = null;
    }

    public Object get(Token name) {
        if (values.containsKey(name.rawText)) {
            return values.get(name.rawText);
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.rawText + "'.");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.rawText)) {
            values.put(name.rawText, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.rawText + "'.");
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.rawText, value);
    }

    public Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
            if (environment == null) {
                return null;
            }
        }

        return environment;
    }
}
