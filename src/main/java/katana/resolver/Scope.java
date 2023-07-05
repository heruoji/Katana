package katana.resolver;

import java.util.HashMap;
import java.util.Map;

public class Scope {
    private Map<String, Boolean> scope = new HashMap<>();

    public void put(String variable, Boolean isResolvedInitializer) {
        this.scope.put(variable, isResolvedInitializer);
    }

    public boolean declaredVariable(String variable) {
        return this.scope.containsKey(variable);
    }

    public Boolean get(String variable) {
        return scope.get(variable);
    }
}
