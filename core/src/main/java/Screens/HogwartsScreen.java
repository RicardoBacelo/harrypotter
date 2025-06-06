package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.bd2r.game.CollisionMap;
import com.bd2r.game.MainGame;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.components.AnimationComponent;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.SpriteComponent;
import com.bd2r.game.ecs.components.VelocityComponent;
import com.bd2r.game.ecs.systems.RenderSystem;

public class HogwartsScreen implements Screen {

    private final MainGame game;
    private final Entity player;
    private final Texture playerTexture;

    private final EntityManager entityManager;
    private final RenderSystem renderSystem;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture mapTexture;
    private int mapWidth, mapHeight;
    private CollisionMap collisionMap;

    public HogwartsScreen(MainGame game, Entity player, Texture playerTexture) {
        this.game = game;
        this.player = player;
        this.playerTexture = playerTexture;
        this.entityManager = new EntityManager();
        this.renderSystem = new RenderSystem();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // ⚠️ Usa o ficheiro .tmx com a camada de colisões chamada "Collisions"
        collisionMap = new CollisionMap("hogwarts.tmx", "Collisions");

        // ⚠️ Troca pela imagem que representa o castelo
        mapTexture = new Texture(Gdx.files.internal("hogwarts.jpg"));
        mapWidth = mapTexture.getWidth();
        mapHeight = mapTexture.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        if (sprite != null) {
            sprite.region = new TextureRegion(playerTexture, 0, 0, 32, 32);
            sprite.scale = 2.5f;
        }

        PositionComponent pos = player.getComponent(PositionComponent.class);
        if (pos != null) {
            pos.x = 500; // ajusta a posição inicial dentro do castelo
            pos.y = 200;
        }

        if (player.getComponent(VelocityComponent.class) == null) {
            player.addComponent(new VelocityComponent(0, 0, 100));
        }

        AnimationComponent anim = player.getComponent(AnimationComponent.class);
        if (anim != null && sprite != null) {
            anim.setDirection("down");
            sprite.region = anim.getCurrentFrame();
        }

        entityManager.addEntity(player);
    }

    @Override
    public void render(float delta) {
        handleInput();

        AnimationComponent anim = player.getComponent(AnimationComponent.class);
        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        if (anim != null && sprite != null) {
            anim.update(delta);
            sprite.region = anim.getCurrentFrame();
        }

        PositionComponent pos = player.getComponent(PositionComponent.class);
        VelocityComponent vel = player.getComponent(VelocityComponent.class);

        if (pos != null && vel != null && sprite != null) {
            float nextX = pos.x + vel.vx * delta;
            float nextY = pos.y + vel.vy * delta;

            // 🧱 Dimensões do sprite ajustadas pela escala
            float spriteWidth = 32 * sprite.scale;
            float spriteHeight = 32 * sprite.scale;

            // 💥 Colisões
            if (!collisionMap.isBlocked(nextX, pos.y, spriteWidth, spriteHeight)) {
                pos.x = nextX;
            }
            if (!collisionMap.isBlocked(pos.x, nextY, spriteWidth, spriteHeight)) {
                pos.y = nextY;
            }

            // Limites do mapa
            pos.x = Math.max(0, Math.min(pos.x, mapWidth - spriteWidth));
            pos.y = Math.max(0, Math.min(pos.y, mapHeight - spriteHeight));
        }

        // Atualiza posição da câmara
        if (pos != null) {
            camera.position.set(pos.x + 16, pos.y + 16, 0);
        }

        clampCameraPosition();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());
        batch.end();
    }

    private void handleInput() {
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        AnimationComponent anim = player.getComponent(AnimationComponent.class);

        if (vel == null || anim == null) return;

        vel.vx = 0;
        vel.vy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vel.vy = 100;
            anim.setDirection("up");
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vel.vy = -100;
            anim.setDirection("down");
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            vel.vx = -100;
            anim.setDirection("left");
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vel.vx = 100;
            anim.setDirection("right");
        }
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
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (mapTexture != null) mapTexture.dispose();
    }
}
