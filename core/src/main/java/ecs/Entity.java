package ecs;

import java.util.HashMap;
import java.util.Map;

public class Entity {
    private final Map<Class<?>, Object> components = new HashMap<>();

    public <T> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    public <T> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    public boolean hasComponent(Class<?> componentClass) {
        return components.containsKey(componentClass);
    }

    public void removeComponent(Class<?> componentClass) {
        components.remove(componentClass);
    }
}

