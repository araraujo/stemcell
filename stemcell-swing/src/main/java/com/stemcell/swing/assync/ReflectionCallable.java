package com.stemcell.swing.assync;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Callable que localiza método e executa método via reflection
 */
class ReflectionCallable implements Callable {

    private Object target;
    private String methodName;
    private Object[] args;

    public ReflectionCallable(Object target, String methodName, Object[] args) {
        this.target = target;
        this.methodName = methodName;
        this.args = (args == null) ? null : Arrays.copyOf(args, args.length);
    }

    @Override
    public Object call() throws Exception {
        Method methodFound = null;

        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == args.length) {
                methodFound = method;
            }
        }

        if (methodFound == null) {
            throw new IllegalArgumentException("Método não encontrado: " + methodName);
        }

        final Method m = methodFound;

        return m.invoke(target, args);
    }
}
