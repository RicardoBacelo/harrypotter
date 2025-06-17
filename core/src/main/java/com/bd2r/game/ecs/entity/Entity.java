package com.bd2r.game.ecs.entity;

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
}
