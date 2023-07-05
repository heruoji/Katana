package katana.interpreter;

import java.util.List;

public interface KatanaCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
