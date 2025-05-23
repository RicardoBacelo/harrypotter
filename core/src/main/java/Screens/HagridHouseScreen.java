package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.bd2r.game.MainGame;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.SpriteComponent;
import com.bd2r.game.ecs.systems.RenderSystem;

public class HagridHouseScreen implements Screen {

    private final MainGame game;
    private final Entity player;
    private final EntityManager entityManager;
    private final RenderSystem renderSystem;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture mapTexture;
    private Texture playerTexture;
    private int mapWidth, mapHeight;

    public HagridHouseScreen(MainGame game, Entity player) {
        this.game = game;
        this.player = player;
        this.entityManager = new EntityManager();
        this.entityManager.addEntity(player);
        this.renderSystem = new RenderSystem();
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

            // Centraliza a câmara
            camera.position.set(pos.x + 16, pos.y + 16, 0);
            camera.update();
        }

        entityManager.addEntity(player);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        clampCameraPosition();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());
        batch.end();
    }

    private void clampCameraPosition() {
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        float minX = halfWidth;
        float maxX = mapWidth - halfWidth;
        float minY = halfHeight;
        float maxY = mapHeight - halfHeight;

        camera.position.x = Math.max(minX, Math.min(camera.position.x, maxX));
        camera.position.y = Math.max(minY, Math.min(camera.position.y, maxY));
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
    }
}




