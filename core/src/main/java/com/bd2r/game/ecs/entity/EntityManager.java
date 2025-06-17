package com.bd2r.game.ecs.entity;

import java.util.ArrayList;
import java.util.List;

public class EntityManager {
    private final List<Entity> entities;

    public EntityManager() {
        this.entities = new ArrayList<>();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
