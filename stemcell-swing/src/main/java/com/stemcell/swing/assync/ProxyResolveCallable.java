package com.stemcell.swing.assync;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.Callable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Callable que constrói proxy que localiza método  e chama schedule num invoker
 */
class ProxyResolveCallable<T> implements Callable, MethodInterceptor, TargetSource {
    private T target;
    private Method method;
    private Object[] args;
    private AssyncInvoker invoker;

    public ProxyResolveCallable(T target, AssyncInvoker invoker) {
        this.target = target;
        this.invoker = invoker;
    }

    @Override
    public Object call() throws Exception {
        if (method == null) {
            throw new IllegalStateException("Método assíncrono não foi executado. Use schedule().<metodo>(argumentos)");
        }

        return method.invoke(target, args);
    }

    public T getProxy() {
        if (target == null) {
            throw new IllegalStateException("Não é possível usar invoke() quando o target do AssyncInvoker não é definido");
        } else {
            ProxyFactory factory = new ProxyFactory();
            factory.addAdvice(this);
            factory.setTargetSource(this);
            return (T) factory.getProxy();
        }
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        this.method = mi.getMethod();
        this.args = (mi.getArguments() == null) ? null : Arrays.copyOf(mi.getArguments(), mi.getArguments().length);
        this.invoker.schedule();
        return null;
    }

    @Override
    public Class<?> getTargetClass() {
        if (target.getClass().getSuperclass().equals(Proxy.class)) {
            return target.getClass().getInterfaces()[0];
        } else {
            return target.getClass();
        }
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public Object getTarget() throws Exception {
        return target;
    }

    @Override
    public void releaseTarget(Object o) throws Exception {
        
    }
    
}
