
package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.bd2r.game.MainGame;

public class CongratulationsScreen implements Screen {
    private final MainGame game;
    private Stage stage;
    private Skin skin;
    private Music music;

    public CongratulationsScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Load and play victory music
        music = Gdx.audio.newMusic(Gdx.files.internal("music/StartMenuSong.mp3"));
        music.setLooping(true);
        music.setVolume(1f);
        music.play();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load the skin
        skin = new Skin(Gdx.files.internal("pixthulhu-ui.json"));

        // Create main table
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Add congratulations title
        Label congratsLabel = new Label("Congratulations!", skin, "title");
        mainTable.add(congratsLabel).pad(30).row();

        // Add message
        Label messageLabel = new Label("You have completed the game!", skin);
        mainTable.add(messageLabel).pad(20).row();

        // Create buttons table
        Table buttonTable = new Table();


        // Play Again button
        TextButton playAgainButton = new TextButton("Play Again", skin);
        playAgainButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                music.stop();
                game.setScreen(new GameScreen(game));
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
        buttonTable.defaults().pad(10).width(350).height(100);
        buttonTable.add(playAgainButton).row();
        buttonTable.add(exitButton).row();

        // Add button table to main table
        mainTable.add(buttonTable);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (music != null) {
            music.dispose();
        }
    }
}


