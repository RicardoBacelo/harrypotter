package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bd2r.game.MainGame;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.systems.RenderSystem;

public class HagridHouseScreen implements Screen{
    private final MainGame game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture mapTexture;
    private final RenderSystem renderSystem;
    private final EntityManager entityManager;
    private int mapWidth, mapHeight;


    public HagridHouseScreen(MainGame game) {
        this.game = game;
        this.renderSystem = new RenderSystem();
        this.entityManager = new EntityManager();
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (batch != null && mapTexture != null) {
            camera.update();
            batch.setProjectionMatrix(camera.combined);

            batch.begin();
            batch.draw(mapTexture, 0, 0);
            renderSystem.render(batch, entityManager.getEntities());
            batch.end();
        }

    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
        if (mapTexture != null) {
            mapTexture.dispose();
            mapTexture = null;
        }

    }

    @Override public void show() {
        // Initialize all resources when the screen is shown
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Load the texture
        mapTexture = new Texture(Gdx.files.internal("casa.jpg"));
        mapWidth = mapTexture.getWidth();
        mapHeight = mapTexture.getHeight();

        // Center the camera on the map
        camera.position.set(mapWidth/2f, mapHeight/2f, 0);
        camera.update();

    }
    @Override public void resize(int width, int height) {
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }

    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}


