package factory;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ecs.Entity;
import ecs.components.PositionComponent;
import ecs.components.SpriteComponent;
import ecs.components.VelocityComponent;

public class EntityFactory {

    public static Entity createPlayer(float x, float y, TextureRegion sprite) {
        Entity player = new Entity();
        player.addComponent(new PositionComponent(x, y));
        player.addComponent(new VelocityComponent(0, 0));
        player.addComponent(new SpriteComponent(sprite));
        return player;
    }

    public static Entity createOwl(float x, float y, TextureRegion sprite) {
        Entity owl = new Entity();
        owl.addComponent(new PositionComponent(x, y));
        owl.addComponent(new VelocityComponent(0, 0)); // para movimento aleatório
        owl.addComponent(new SpriteComponent(sprite));
        return owl;
    }

    // No futuro podes adicionar:
    // public static Entity createEnemy(...) { ... }
    // public static Entity createCoin(...) { ... }
}
