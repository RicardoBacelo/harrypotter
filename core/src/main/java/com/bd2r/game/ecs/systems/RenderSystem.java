package com.bd2r.game.ecs.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.SpriteComponent;

import java.util.List;

public class RenderSystem {
    public void render(SpriteBatch batch, List<Entity> entities) {
        for (Entity entity : entities) {
            PositionComponent position = entity.getComponent(PositionComponent.class);
            SpriteComponent sprite = entity.getComponent(SpriteComponent.class);

            if (position != null && sprite != null && sprite.region != null) {
                batch.draw(sprite.region,
                    position.x,
                    position.y,
                    sprite.region.getRegionWidth() * sprite.scale,
                    sprite.region.getRegionHeight() * sprite.scale);
            }
        }
    }
}


