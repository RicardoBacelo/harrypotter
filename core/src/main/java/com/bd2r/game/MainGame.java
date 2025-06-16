package com.bd2r.game;

import Screens.GameScreen;
import Screens.MenuScreen;
import com.badlogic.gdx.Game;

public class MainGame extends Game {

    // Inventário partilhado entre todos os ecrãs
    private final Inventory inventory = new Inventory();

    // Permite aceder ao inventário a partir de outros ecrãs
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void create() {
        // Início do jogo - mostra o menu principal
        System.out.println("🟢 MainGame: iniciado");
        setScreen(new MenuScreen(this));
    }
}
