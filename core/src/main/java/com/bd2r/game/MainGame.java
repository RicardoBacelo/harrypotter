package com.bd2r.game;

import com.badlogic.gdx.Game;

public class MainGame extends Game {
    @Override
    public void create() {
        System.out.println("🟢 MainGame: iniciado");
        setScreen(new GameScreen());
    }
}
