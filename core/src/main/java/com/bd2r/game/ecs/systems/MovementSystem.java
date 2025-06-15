package com.bd2r.game.ecs.systems;

import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.components.*;
import com.bd2r.game.pathfinder.Node;

import java.util.List;

public class MovementSystem {

    private static final int TILE_SIZE = 32;

    public void update(List<Entity> entities, float delta, int mapWidth, int mapHeight) {
        for (Entity entity : entities) {
            PositionComponent pos = entity.getComponent(PositionComponent.class);
            VelocityComponent vel = entity.getComponent(VelocityComponent.class);
            PathComponent path = entity.getComponent(PathComponent.class);

            if (pos == null || vel == null) continue;

            if (path != null && !path.path.isEmpty()) {
                Node nextStep = path.path.peek();
                float targetX = nextStep.x * TILE_SIZE;
                float targetY = nextStep.y * TILE_SIZE;

                float speed = vel.speed * delta;
                float dx = targetX - pos.x;
                float dy = targetY - pos.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < speed) {
                    pos.x = targetX;
                    pos.y = targetY;
                    path.path.remove();
                    vel.vx = 0;
                    vel.vy = 0;
                } else {
                    vel.vx = (dx / dist) * vel.speed;
                    vel.vy = (dy / dist) * vel.speed;

                    // 👇 Atualizar a direção da animação com base no movimento
                    AnimationComponent anim = entity.getComponent(AnimationComponent.class);
                    if (anim != null) {
                        if (Math.abs(dx) > Math.abs(dy)) {
                            anim.setDirection(dx > 0 ? "right" : "left");
                        } else {
                            anim.setDirection(dy > 0 ? "up" : "down");
                        }
                    }
                }

            }

            // AQUI é onde estávamos a falhar:
            pos.x += vel.vx * delta;
            pos.y += vel.vy * delta;

            // Limitar dentro dos limites do mapa
            pos.x = Math.max(0, Math.min(pos.x, mapWidth - TILE_SIZE));
            pos.y = Math.max(0, Math.min(pos.y, mapHeight - TILE_SIZE));
        }
    }

}
