package com.bd2r.game.Observer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Screens.GameScreen;
import com.bd2r.game.Inventory;

import java.util.ArrayList;
import java.util.List;

public class CoinManager {
    private final List<Coin> coins = new ArrayList<>();

    public void addCoin(Coin coin, GameScreen player) {
        coins.add(coin);
        //player.registerObserver(coin);
    }

    public void updateAndNotifyCoins(float playerX, float playerY, Inventory inventory) {
        for (Coin coin : coins) {
            coin.update(playerX, playerY, inventory);
        }
    }

    public void render(SpriteBatch batch, Texture texture, float delta) {
        for (Coin coin : coins) {
            coin.render(batch, texture, delta);
        }
        coins.removeIf(Coin::isAnimationFinished);
    }

    public void dispose() {}
}
