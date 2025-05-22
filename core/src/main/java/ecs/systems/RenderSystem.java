package ecs.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ecs.Entity;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.SpriteComponent;

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



