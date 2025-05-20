package com.bd2r.game.Observer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bd2r.game.GameScreen;

import java.util.ArrayList;
import java.util.List;

public class SilverKeyManager {
    private final List<SilverKey> silverKeys = new ArrayList<>();

    public void addSilverKey(SilverKey silverKey, GameScreen player) {
        silverKeys.add(silverKey);
    }

    public void updateAndNotifyKeys(float playerX, float playerY) {
        for (SilverKey silverKey : silverKeys) {
            silverKey.update(playerX, playerY);
        }
    }

    public void render(SpriteBatch batch, Texture texture, float delta) {
        for (SilverKey silverKey: silverKeys) {
            silverKey.render(batch, texture, delta);
        }
        silverKeys.removeIf(SilverKey::isAnimationFinished);
    }

    public void dispose() {}
}
