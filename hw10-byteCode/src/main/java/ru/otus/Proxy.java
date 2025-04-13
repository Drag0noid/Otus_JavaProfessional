package ru.otus;

import java.lang.reflect.*;
import java.util.Arrays;

public class Proxy {
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target, Class<T> interfaceType) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{
                interfaceType
                },
                new LoggingHandler<>(target)
        );
    }

    private static class LoggingHandler<T> implements InvocationHandler {
        private final T target;

        public LoggingHandler(T target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method implMethod = target.getClass()
                    .getMethod(method.getName(), method.getParameterTypes());

            if (implMethod.isAnnotationPresent(Log.class)) {
                StringBuilder log = new StringBuilder("executed method: ")
                        .append(method.getName());
                if (args != null && args.length > 0) {
                    log.append(", params: ").append(
                            String.join(", ", Arrays.stream(args).map(String::valueOf).toArray(String[]::new)));
                }
                System.out.println(log);
            }

            return method.invoke(target, args);
        }
    }
}
