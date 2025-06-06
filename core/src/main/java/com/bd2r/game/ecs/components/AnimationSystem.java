package com.bd2r.game.ecs.systems;

import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.components.AnimationComponent;
import com.bd2r.game.ecs.components.SpriteComponent;

import java.util.List;

public class AnimationSystem {
    public void update(List<Entity> entities, float delta) {
        for (Entity entity : entities) {
            AnimationComponent anim = entity.getComponent(AnimationComponent.class);
            SpriteComponent sprite = entity.getComponent(SpriteComponent.class);

            if (anim != null && sprite != null) {
                anim.update(delta);
                sprite.region = anim.getCurrentFrame();
            }
        }
    }
}
