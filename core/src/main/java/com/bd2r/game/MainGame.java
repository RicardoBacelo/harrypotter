package com.bd2r.game;

import com.badlogic.gdx.Game;

public class MainGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());  // <- isto é obrigatório!
    }
}

