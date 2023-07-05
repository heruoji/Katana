package katana.interpreter;

import katana.model.stmt.FunctionStatement;

import java.util.List;

public class KatanaFunction implements KatanaCallable {
    private final FunctionStatement declaration;
    private final Environment closure;

    private final boolean isInitializer;

    public KatanaFunction(FunctionStatement declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    KatanaFunction bind(KatanaInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new KatanaFunction(declaration, environment, isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).rawText, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (ReturnValue functionReturn) {
            if (isInitializer) {
                return closure.getAt(0, "this");
            }
            return functionReturn.value;
        }

        if (isInitializer) {
            return closure.getAt(0, "this");
        }
        return null;
    }

    @Override
    public int arity() {
        return this.declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.rawText + ">";
    }
}
