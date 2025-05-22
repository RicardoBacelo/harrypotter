package factory;



import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ecs.components.SpriteComponent;
import ecs.Entity;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;

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

