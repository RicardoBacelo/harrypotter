package com.bd2r.game.Observer;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bd2r.game.Inventory;

import java.util.ArrayList;
import java.util.List;

public class WandManager {
    private final List<Wand> wands = new ArrayList<>();

    public void addWand(Wand wand, Screen screen) {
        wands.add(wand);
        //player.registerObserver(wand);
    }

    public void updateAndNotifyWands(float playerX, float playerY, Inventory inventory) {

        for (Wand wand : wands) {
            wand.update(playerX, playerY, inventory);
        }
    }

    public void render(SpriteBatch batch, Texture texture, float delta) {
        for (Wand wand : wands) {
            wand.render(batch, texture, delta);
        }
        wands.removeIf(Wand::isAnimationFinished);
    }

    public void dispose() {}
}
