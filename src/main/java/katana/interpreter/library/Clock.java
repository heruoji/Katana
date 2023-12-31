package katana.interpreter.library;

import katana.interpreter.Interpreter;
import katana.interpreter.KatanaCallable;

import java.util.List;

public class Clock implements KatanaCallable {

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
    }

    @Override
    public String toString() {
        return "<native fn clock>";
    }
}
