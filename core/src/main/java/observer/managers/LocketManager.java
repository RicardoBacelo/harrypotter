package observer.managers;

import Screens.HagridHouseScreen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import items.Locket;
import ui.Inventory;

import java.util.ArrayList;
import java.util.List;

public class LocketManager {
    private final List<Locket> lockets = new ArrayList<>();

    public void addLocket(Locket locket, HagridHouseScreen player) {
        lockets.add(locket);
        //player.registerObserver(coin);
    }

    public void updateAndNotifyLockets(float playerX, float playerY, Inventory inventory) {
        for (Locket locket : lockets) {
            locket.update(playerX, playerY, inventory);
        }
    }

    public void render(SpriteBatch batch, Texture texture, float delta) {
        for (Locket locket : lockets) {
            locket.render(batch, texture, delta);
        }
        lockets.removeIf(Locket::isAnimationFinished);
    }

    public void dispose() {}
}
