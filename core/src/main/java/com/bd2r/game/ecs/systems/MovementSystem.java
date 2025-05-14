package com.bd2r.game.ecs.systems;

import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.VelocityComponent;

import java.util.List;

public class MovementSystem {

    public void update(List<Entity> entities, float deltaTime) {
        for (Entity entity : entities) {
            if (entity.hasComponent(PositionComponent.class) && entity.hasComponent(VelocityComponent.class)) {
                PositionComponent pos = entity.getComponent(PositionComponent.class);
                VelocityComponent vel = entity.getComponent(VelocityComponent.class);

                pos.x += vel.vx * deltaTime;
                pos.y += vel.vy * deltaTime;
            }
        }
    }
}

