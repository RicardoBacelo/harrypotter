package com.bd2r.game.ecs.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.components.AnimationComponent;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.SpriteComponent;

import java.util.List;

public class RenderSystem {
    public void render(SpriteBatch batch, List<Entity> entities) {
        for (Entity entity : entities) {
            PositionComponent pos = entity.getComponent(PositionComponent.class);
            SpriteComponent sprite = entity.getComponent(SpriteComponent.class);
            AnimationComponent anim = entity.getComponent(AnimationComponent.class);

            if (pos != null && sprite != null) {
                if (anim != null) {
                    sprite.region = anim.getCurrentFrame();
                }
                batch.draw(sprite.region, pos.x, pos.y);
            }
        }
    }
}



