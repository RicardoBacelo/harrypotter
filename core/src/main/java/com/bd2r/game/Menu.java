package com.bd2r.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Menu extends Game {
    private Stage stage;
    private Skin skin;


    @Override
    public void create() {

        // Load and play the music
        Music menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/StartMenuSong.mp3"));
        menuMusic.setLooping(true); // Makes the music loop
        menuMusic.setVolume(0.5f); // Sets volume to 50%
        menuMusic.play();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin
        skin = new Skin(Gdx.files.internal("pixthulhu-ui.json"));

        // Create main table
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Add title label
        Label titleLabel = new Label("Harry Potter", skin, "title");
        mainTable.add(titleLabel).pad(50).row();

        // Create buttons table
        Table buttonTable = new Table();

        // Play button
        TextButton playButton = new TextButton("Play", skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameScreen gameScreen = new GameScreen();
                setScreen(gameScreen);

            }
        });

        // Exit button
        TextButton exitButton = new TextButton("Exit", skin);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Add buttons to button table with spacing
        buttonTable.defaults().pad(10).width(200).height(100);
        buttonTable.add(playButton).row();
        buttonTable.add(exitButton).row();

        // Add button table to main table
        mainTable.add(buttonTable);

        // Optional: Add version label at bottom
        Label versionLabel = new Label("v1.0", skin);
        mainTable.add(versionLabel).padTop(50).row();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        if (screen != null) {
            super.render(); // This will render the current screen
        } else {

            // Clear screen with a dark background
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            float delta = Gdx.graphics.getDeltaTime();
            stage.act(delta);
            stage.draw();
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
