package observer.managers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import Screens.GameScreen;
import ui.Inventory;
import items.Coin;

import java.util.ArrayList;
import java.util.List;

public class CoinManager {
    //Array onde são guardadas as moedas
    private final List<Coin> coins = new ArrayList<>();

    //Adiciona moeda à lista
    public void addCoin(Coin coin, GameScreen player) {
        coins.add(coin);
        //player.registerObserver(coin);
    }

    //Notifica e atualiza se moeda foi apanhada
    public void updateAndNotifyCoins(float playerX, float playerY, Inventory inventory) {
        for (Coin coin : coins) {
            coin.update(playerX, playerY, inventory);
        }
    }

    //Desenha a moeda na tela
    public void render(SpriteBatch batch, Texture texture, float delta) {
        for (Coin coin : coins) {
            coin.render(batch, texture, delta);
        }
        coins.removeIf(Coin::isAnimationFinished); //Remove moedas após apanhadas
    }

    public void dispose() {}
}
