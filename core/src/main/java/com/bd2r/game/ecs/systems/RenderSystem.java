package com.bd2r.game.ecs.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.components.AnimationComponent;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.SpriteComponent;

import java.util.List;

public class RenderSystem {

    private final SpriteBatch batch;

    public RenderSystem(SpriteBatch batch) {
        this.batch = batch;
    }

    public void render(List<Entity> entities) {
        batch.begin();
        for (Entity entity : entities) {
            if (entity.hasComponent(PositionComponent.class) &&
                entity.hasComponent(SpriteComponent.class)) {

                PositionComponent pos = entity.getComponent(PositionComponent.class);
                SpriteComponent sprite = entity.getComponent(SpriteComponent.class);
                TextureRegion region = sprite.region;

                if (entity.hasComponent(AnimationComponent.class)) {
                    AnimationComponent anim = entity.getComponent(AnimationComponent.class);
                    region = anim.getCurrentFrame();
                }

                batch.draw(region, pos.x, pos.y);
            }
        }

        batch.end();
    }
}



