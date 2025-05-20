package com.bd2r.game;

import Screens.GameScreen;
import Screens.MenuScreen;
import com.badlogic.gdx.Game;

public class MainGame extends Game {

    private final Inventory inventory = new Inventory();

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void create() {
        System.out.println("🟢 MainGame: iniciado");
        setScreen(new MenuScreen(this));
    }
}
