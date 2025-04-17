package ru.otus;

import java.lang.reflect.*;
import java.util.*;

public class ProxyFactory {
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target, Class<T> interfaceType) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                new LoggingHandler<>(target)
        );
    }

    private static class LoggingHandler<T> implements InvocationHandler {
        private final T target;
        private final Set<MethodKey> loggableMethods;

        public LoggingHandler(T target) {
            this.target = target;
            this.loggableMethods = findLoggableMethods(target.getClass());
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (loggableMethods.contains(new MethodKey(method))) {
                StringBuilder log = new StringBuilder("executed method: ")
                        .append(method.getName());
                if (args != null && args.length > 0) {
                    log.append(", params: ")
                            .append(String.join(", ", Arrays.stream(args).map(String::valueOf).toArray(String[]::new)));
                }
                System.out.println(log);
            }
            return method.invoke(target, args);
        }

        private Set<MethodKey> findLoggableMethods(Class<?> clazz) {
            Set<MethodKey> methods = new HashSet<>();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Log.class)) {
                    methods.add(new MethodKey(method));
                }
            }
            return methods;
        }

        private static class MethodKey {
            private final String name;
            private final List<Class<?>> parameterTypes;

            MethodKey(Method method) {
                this.name = method.getName();
                this.parameterTypes = Arrays.asList(method.getParameterTypes());
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof MethodKey)) return false;
                MethodKey that = (MethodKey) o;
                return Objects.equals(name, that.name) &&
                        Objects.equals(parameterTypes, that.parameterTypes);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, parameterTypes);
            }
        }
    }
}
