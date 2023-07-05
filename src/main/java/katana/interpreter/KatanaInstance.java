package katana.interpreter;

import katana.model.token.Token;

import java.util.HashMap;
import java.util.Map;

public class KatanaInstance {
    private KatanaClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    public KatanaInstance(KatanaClass klass) {
        this.klass = klass;
    }

    Object get(Token name) {
        if (fields.containsKey(name.rawText)) {
            return fields.get(name.rawText);
        }

        KatanaFunction method = klass.findMethod(name.rawText);
        if (method != null) {
            return method.bind(this);
        }

        throw new RuntimeError(name, "Undefined property '" + name.rawText + "'.");
    }

    public void set(Token name, Object value) {
        fields.put(name.rawText, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
