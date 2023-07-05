package katana.interpreter;

import java.util.List;
import java.util.Map;

public class KatanaClass implements KatanaCallable {
    String name;
    KatanaClass superClass;
    private final Map<String, KatanaFunction> methods;

    public KatanaClass(String name, KatanaClass superClass, Map<String, KatanaFunction> methods) {
        this.name = name;
        this.superClass = superClass;
        this.methods = methods;
    }

    KatanaFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superClass != null) {
            return superClass.findMethod(name);
        }

        return null;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        KatanaInstance instance = new KatanaInstance(this);
        KatanaFunction initializer = findMethod("constructor");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity() {
        KatanaFunction initializer = findMethod("constructor");
        if (initializer == null) {
            return 0;
        }
        return initializer.arity();
    }
}
