package com.bd2r.game.Observer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Screens.GameScreen;
import java.util.ArrayList;
import java.util.List;
import com.bd2r.game.Inventory;

public class GoldenKeyManager {
    private final List<GoldenKey> goldenKeys = new ArrayList<>();

    public void addGoldenKey(GoldenKey goldenKey, GameScreen player) {
        goldenKeys.add(goldenKey);
    }

    public void updateAndNotifyKeys(float playerX, float playerY, Inventory inventory) {
        for (GoldenKey goldenKey : goldenKeys) {
            goldenKey.update(playerX, playerY, inventory);
        }
    }

    public void render(SpriteBatch batch, Texture texture, float delta) {
        for (GoldenKey goldenKey: goldenKeys) {
            goldenKey.render(batch, texture, delta);
        }
        goldenKeys.removeIf(GoldenKey::isAnimationFinished);
    }

    public void dispose() {}
}
