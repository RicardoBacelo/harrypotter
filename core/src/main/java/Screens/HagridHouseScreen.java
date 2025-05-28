package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
<<<<<<< Updated upstream
import core.MainGame;
import ecs.EntityManager;
import ecs.systems.RenderSystem;
=======
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.bd2r.game.Inventory;
import com.bd2r.game.MainGame;
import com.bd2r.game.Observer.Coin;
import com.bd2r.game.Observer.CoinManager;
import com.bd2r.game.Observer.Locket;
import com.bd2r.game.Observer.LocketManager;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.SpriteComponent;
import com.bd2r.game.ecs.systems.RenderSystem;

public class HagridHouseScreen implements Screen {
>>>>>>> Stashed changes

public class HagridHouseScreen implements Screen{
    private final MainGame game;
<<<<<<< Updated upstream
=======
    private final Entity player;
    private final EntityManager entityManager;
    private final RenderSystem renderSystem;
    private SpriteBatch batch;

    private LocketManager locketManager;
    private Texture locketTexture;
    private Locket locket;

    private final Inventory inventory;
    private BitmapFont font;
    private Texture whitePixel;
>>>>>>> Stashed changes
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture mapTexture;
    private final RenderSystem renderSystem;
    private final EntityManager entityManager;
    private int mapWidth, mapHeight;


    public HagridHouseScreen(MainGame game) {
        this.game = game;
        this.renderSystem = new RenderSystem();
<<<<<<< Updated upstream
        this.entityManager = new EntityManager();
=======
        this.inventory = game.getInventory();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Carrega os gráficos
        mapTexture = new Texture(Gdx.files.internal("casa.jpg"));
        playerTexture = new Texture(Gdx.files.internal("hero1.png"));
        mapWidth = mapTexture.getWidth();
        mapHeight = mapTexture.getHeight();

        //inventory = new Inventory();
        font = new BitmapFont();
        font.getData().setScale(1.0f);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        locketTexture = new Texture(Gdx.files.internal("locket.png"));
        locketManager = new LocketManager();
        locket = new Locket(390, 770);
        locketManager.addLocket(locket, this);


        // Define sprite do jogador
        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        if (sprite != null) {
            sprite.region = new TextureRegion(playerTexture, 0, 0, 32, 32);
            sprite.scale = 2.5f;// frame base
        }

        // Posição inicial do jogador
        PositionComponent pos = player.getComponent(PositionComponent.class);
        if (pos != null) {
            pos.x = 500;
            pos.y = 100;

            locketManager.updateAndNotifyLockets(pos.x, pos.y, inventory);

            // Centraliza a câmara
            camera.position.set(pos.x + 16, pos.y + 16, 0);
            camera.update();
        }

        entityManager.addEntity(player);
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======
        locketManager.updateAndNotifyLockets(pos.x, pos.y, inventory);

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
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (mapTexture != null) mapTexture.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (locketTexture != null) locketTexture.dispose();
    }
>>>>>>> Stashed changes
}


