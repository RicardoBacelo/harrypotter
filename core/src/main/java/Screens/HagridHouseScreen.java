package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import core.MainGame;
import ecs.EntityManager;
import ecs.systems.RenderSystem;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ui.Inventory;
import core.MainGame;
import items.Coin;
import com.bd2r.game.Observer.Locket;
import com.bd2r.game.Observer.LocketManager;
import ecs.Entity;
import ecs.EntityManager;
import ecs.components.PositionComponent;
import ecs.components.SpriteComponent;
import ecs.systems.RenderSystem;

public class HagridHouseScreen implements Screen{
    private final MainGame game;
    private final EntityManager entityManager;
    private final RenderSystem renderSystem;
    private SpriteBatch batch;

    private LocketManager locketManager;
    private Texture locketTexture;
    private Locket locket;
    private final Entity player = new Entity();
    private final Inventory inventory;
    private Texture whitePixel;
    private OrthographicCamera camera;
    private Texture mapTexture, playerTexture;
    private int mapWidth, mapHeight;


    public HagridHouseScreen(MainGame game) {
        this.game = game;
        this.renderSystem = new RenderSystem();
        this.entityManager = new EntityManager();
        this.inventory = game.getInventory();
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        PositionComponent pos = player.getComponent(PositionComponent.class);

        if (batch != null && mapTexture != null) {
            camera.update();
            batch.setProjectionMatrix(camera.combined);

            batch.begin();
            batch.draw(mapTexture, 0, 0);
            renderSystem.render(batch, entityManager.getEntities());
            batch.end();
        }

    }

    @Override public void show() {
        PositionComponent position = new PositionComponent();
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
        locketManager.updateAndNotifyLockets(position.x,  position.y, inventory);

        batch.begin();
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());
        float origW = locketTexture.getWidth();
        float origH = locketTexture.getHeight();
        float scale = 0.1f;
        float locketX = locket.getX();
        float locketY = locket.getY();

        batch.draw(locketTexture,
            locketX - (origW * scale) / 2,
            locketY - (origH * scale) / 2,
            origW * scale,
            origH * scale
        );
        batch.end();
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
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (mapTexture != null) mapTexture.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (locketTexture != null) locketTexture.dispose();
    }
}


