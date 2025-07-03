package ru.otus.appcontainer;

import ru.otus.appcontainer.api.AppComponent;
import ru.otus.appcontainer.api.AppComponentsContainer;
import ru.otus.appcontainer.api.AppComponentsContainerConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class AppComponentsContainerImpl implements AppComponentsContainer {

    private final List<Object> components = new ArrayList<>();
    private final Map<String, Object> nameToComponent = new HashMap<>();

    public AppComponentsContainerImpl(Class<?> configClass) {
        checkConfig(configClass);
        processConfig(configClass);
    }

    private void checkConfig(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(AppComponentsContainerConfig.class)) {
            throw new IllegalArgumentException("Not a config class: " + configClass.getName());
        }
    }

    private void processConfig(Class<?> configClass) {
        Object configInstance = createInstance(configClass);

        Method[] allMethods = configClass.getDeclaredMethods();
        List<Method> appMethods = new ArrayList<>();
        Map<Method, AppComponent> methodToComponent = new HashMap<>();

        for (Method method : allMethods) {
            if (method.isAnnotationPresent(AppComponent.class)) {
                AppComponent comp = method.getAnnotation(AppComponent.class);
                appMethods.add(method);
                methodToComponent.put(method, comp);
            }
        }

        appMethods.sort(Comparator.comparingInt(m -> methodToComponent.get(m).order()));

        for (Method method : appMethods) {
            AppComponent annotation = methodToComponent.get(method);
            String name = annotation.name();

            if (nameToComponent.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate component name: " + name);
            }

            Object[] args = Arrays.stream(method.getParameterTypes())
                    .map(this::resolveDependency)
                    .toArray();

            try {
                method.setAccessible(true);
                Object component = method.invoke(configInstance, args);
                components.add(component);
                nameToComponent.put(name, component);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create component: " + name, e);
            }
        }
    }

    private Object createInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate class: " + clazz.getName(), e);
        }
    }

    private Object resolveDependency(Class<?> type) {
        List<Object> matches = components.stream()
                .filter(c -> type.isAssignableFrom(c.getClass()))
                .toList();

        if (matches.isEmpty()) {
            throw new RuntimeException("No component found for type: " + type.getName());
        }
        if (matches.size() > 1) {
            throw new RuntimeException("Multiple components found for type: " + type.getName());
        }

        return matches.get(0);
    }

    @Override
    public <C> C getAppComponent(Class<C> componentClass) {
        return componentClass.cast(resolveDependency(componentClass));
    }

    @Override
    public <C> C getAppComponent(String componentName) {
        Object component = nameToComponent.get(componentName);
        if (component == null) {
            throw new RuntimeException("No component found with name: " + componentName);
        }
        return (C) component;
    }
}
