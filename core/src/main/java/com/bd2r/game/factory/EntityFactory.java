package com.bd2r.game.factory;



import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.bd2r.game.ecs.components.SpriteComponent;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.VelocityComponent;

public class EntityFactory {

    public static Entity createPlayer(float x, float y, TextureRegion sprite) {
        Entity player = new Entity();
        player.addComponent(new PositionComponent(x, y));
        player.addComponent(new VelocityComponent(0, 0));
        player.addComponent(new SpriteComponent(sprite));
        return player;
    }

    // No futuro podes adicionar:
    // public static Entity createEnemy(...) { ... }
    // public static Entity createCoin(...) { ... }
}

