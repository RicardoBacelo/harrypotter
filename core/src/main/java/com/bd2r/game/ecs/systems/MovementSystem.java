package com.bd2r.game.ecs.systems;

import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.VelocityComponent;
import com.bd2r.game.ecs.components.PathComponent;
import com.bd2r.game.pathfinder.Node;

import java.util.List;

public class MovementSystem {
    private static final int TILE_SIZE = 32;
    private static final float SPEED = 100f;

    public void update(List<Entity> entities, float delta, int mapWidth, int mapHeight) {
        for (Entity entity : entities) {
            PositionComponent pos = entity.getComponent(PositionComponent.class);
            VelocityComponent vel = entity.getComponent(VelocityComponent.class);
            PathComponent path = entity.getComponent(PathComponent.class);

            if (pos == null || vel == null) continue;

            if (path != null && !path.path.isEmpty()) {
                Node nextStep = path.path.get(0);
                float targetX = nextStep.x * TILE_SIZE;
                float targetY = nextStep.y * TILE_SIZE;


                float dx = targetX - pos.x;
                float dy = targetY - pos.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance > 1f) {
                    vel.vx = (dx / distance) * SPEED;
                    vel.vy = (dy / distance) * SPEED;

                    // Update position
                    pos.x += vel.vx * delta;
                    pos.y += vel.vy * delta;


                    // Check if we've reached the current waypoint
                    float newDistance = (float) Math.sqrt(
                        Math.pow(targetX - pos.x, 2) +
                            Math.pow(targetY - pos.y, 2)
                    );

                    if (newDistance < 1f) {
                        // Snap to waypoint position
                        pos.x = targetX;
                        pos.y = targetY;
                        // Remove this waypoint as we've reached it
                        path.path.remove(0);
                    }
                } else {
                    // We're very close to waypoint, snap to it and remove it
                    pos.x = targetX;
                    pos.y = targetY;
                    path.path.remove(0);
                }

                // If we've reached the end of the path, stop moving
                    if (path.path.isEmpty()) {
                        vel.vx = 0;
                        vel.vy = 0;
                    }
                } else {
                    // Regular movement without path
                    pos.x += vel.vx * delta;
                    pos.y += vel.vy * delta;

                    // Clamp to map boundaries
                    pos.x = Math.max(0, Math.min(pos.x, mapWidth - TILE_SIZE));
                    pos.y = Math.max(0, Math.min(pos.y, mapHeight - TILE_SIZE));
                }

            }
        }
    }

