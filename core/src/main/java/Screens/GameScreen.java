package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bd2r.game.Inventory;
import com.bd2r.game.MainGame;
import com.bd2r.game.MapLoader;
import com.bd2r.game.Observer.*;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.components.AnimationComponent;
import com.bd2r.game.ecs.components.PathComponent;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.VelocityComponent;
import com.bd2r.game.ecs.systems.AnimationSystem;
import com.bd2r.game.ecs.systems.MovementSystem;
import com.bd2r.game.ecs.systems.RenderSystem;
import com.bd2r.game.factory.EntityFactory;
import com.bd2r.game.pathfinder.AStarPathfinder;
import com.bd2r.game.pathfinder.Node;

import java.util.List;

public class GameScreen implements Screen {
    private final EntityManager entityManager = new EntityManager();
    private final MovementSystem movementSystem = new MovementSystem();
    private final AnimationSystem animationSystem = new AnimationSystem();
    private final RenderSystem renderSystem = new RenderSystem();

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture playerTexture, mapTexture;
    private Entity player;

    private OrthographicCamera camera;
    private static final int TILE_SIZE = 32;
    private int mapWidth, mapHeight;

    private CoinManager coinManager;
    private Texture coinTexture;

    private SilverKeyManager silverKeyManager;
    private Texture silverKeyTexture;

    private GoldenKeyManager goldenKeyManager;
    private Texture goldenKeyTexture;

    private final Inventory inventory;
    private BitmapFont font;
    private Texture whitePixel;
    private final MainGame game;

    public GameScreen(MainGame game) {
        this.game = game;
        this.inventory = game.getInventory();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(1.0f);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        mapTexture = new Texture(Gdx.files.internal("mundo.png"));
        mapWidth = mapTexture.getWidth();
        mapHeight = mapTexture.getHeight();

        playerTexture = new Texture(Gdx.files.internal("hero1.png"));

        player = EntityFactory.createPlayer(485, 60, playerTexture);
        entityManager.addEntity(player);

        coinManager = new CoinManager();
        coinTexture = new Texture(Gdx.files.internal("coin.png"));
        coinManager.addCoin(new Coin(500, 100), this);
        coinManager.addCoin(new Coin(400, 150), this);

        silverKeyManager = new SilverKeyManager();
        silverKeyTexture = new Texture(Gdx.files.internal("House_Key.png"));
        silverKeyManager.addSilverKey(new SilverKey(500, 150), this);

        goldenKeyManager = new GoldenKeyManager();
        goldenKeyTexture = new Texture(Gdx.files.internal("Castle_Key.png"));
        goldenKeyManager.addGoldenKey(new GoldenKey(750, 150), this);
    }

    @Override
    public void render(float delta) {
        try {
            animationSystem.update(entityManager.getEntities(), delta);
            handleInput();
            movementSystem.update(entityManager.getEntities(), delta, mapWidth, mapHeight);

            PositionComponent pos = player.getComponent(PositionComponent.class);
            coinManager.updateAndNotifyCoins(pos.x, pos.y, inventory);
            silverKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            goldenKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);

            camera.position.set(pos.x + 16, pos.y + 16, 0);
            clampCameraPosition();
            camera.update();

            Gdx.gl.glClearColor(0.1f, 0.1f, 0.3f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            if (Gdx.input.justTouched()) {
                try {
                    float worldX = camera.position.x - camera.viewportWidth / 2 + Gdx.input.getX();
                    float worldY = camera.position.y + camera.viewportHeight / 2 - Gdx.input.getY();

                    int tileX = (int) (worldX / TILE_SIZE);
                    int tileY = (int) (worldY / TILE_SIZE);

                    int startX = (int) (pos.x / TILE_SIZE);
                    int startY = (int) (pos.y / TILE_SIZE);

                    AStarPathfinder pathfinder = new AStarPathfinder(MapLoader.loadMap("mapa.txt"));
                    List<Node> path = pathfinder.findPath(startX, startY, tileX, tileY);

                    if (path != null && !path.isEmpty()) {
                        PathComponent pathComp = player.getComponent(PathComponent.class);
                        if (pathComp == null) {
                            pathComp = new PathComponent();
                            player.addComponent(pathComp);
                        }
                        pathComp.setPath(path);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Desenhar mapa e entidades
            batch.begin();
            checkTriggers(pos.x, pos.y);
            batch.draw(mapTexture, 0, 0);
            renderSystem.render(batch, entityManager.getEntities());
            coinManager.render(batch, coinTexture, delta);
            silverKeyManager.render(batch, silverKeyTexture, delta);
            goldenKeyManager.render(batch, goldenKeyTexture, delta);
            batch.end();

            // 🔴 Desenhar ponto vermelho na entrada de Hogwarts
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.circle(7 * TILE_SIZE + 16, 34 * TILE_SIZE + 16, 6);
            shapeRenderer.end();

            // Desenhar inventário
            batch.begin();
            batch.setProjectionMatrix(camera.combined);

            float inventoryX = camera.position.x + (camera.viewportWidth / 2) - 200;
            float inventoryY = camera.position.y - (camera.viewportHeight / 2) + 100;

            batch.setColor(0f, 0f, 0f, 0.5f);
            batch.draw(whitePixel, inventoryX - 16, inventoryY - 88, 180, 100);
            batch.setColor(Color.WHITE);

            font.draw(batch, "Inventário", inventoryX, inventoryY);
            font.draw(batch, inventory.getItemCount(ItemType.COIN) + " x Coins", inventoryX, inventoryY - 24);
            font.draw(batch, inventory.getItemCount(ItemType.SILVER_KEY) + " x Silver Keys", inventoryX, inventoryY - 48);
            font.draw(batch, inventory.getItemCount(ItemType.GOLDEN_KEY) + " x Golden Keys", inventoryX, inventoryY - 72);

            batch.end();

        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error in render", e);
        }
    }


    private void handleInput() {
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        AnimationComponent anim = player.getComponent(AnimationComponent.class);

        vel.vx = 0;
        vel.vy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            vel.vx = -100;
            anim.setDirection("left");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vel.vx = 100;
            anim.setDirection("right");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vel.vy = 100;
            anim.setDirection("up");
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vel.vy = -100;
            anim.setDirection("down");
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

    private void checkTriggers(float x, float y) {
        int tileX = (int) (x / TILE_SIZE);
        int tileY = (int) (y / TILE_SIZE);

        // Debug: ver em que tile estás
        System.out.println("tileX = " + tileX + ", tileY = " + tileY);

        PathComponent pathComp = player.getComponent(PathComponent.class);

        if (pathComp != null && pathComp.path.isEmpty()) {

            // Porta para a casa do Hagrid (ex: tile 16,5)
            if (tileX == 7 && tileY == 34 && inventory.getItemCount(ItemType.SILVER_KEY) > 0) {
                if (batch.isDrawing()) batch.end();
                game.setScreen(new HagridHouseScreen(game, player, playerTexture));
                dispose();
            }

            // Porta para Hogwarts (ex: tile 23,10) — AJUSTA os valores com base no print do terminal
            if (tileX == 7 && tileY == 41 && inventory.getItemCount(ItemType.GOLDEN_KEY) > 0) {
                if (batch.isDrawing()) batch.end();
                game.setScreen(new HogwartsScreen(game, player, playerTexture));
                dispose();
            }

            // (Opcional) Se quiseres mostrar aviso ao tentar entrar sem chave:
            if (tileX == 23 && tileY == 10 && inventory.getItemCount(ItemType.GOLDEN_KEY) == 0) {
                System.out.println("🚪 Porta trancada. Precisas da Golden Key.");
            }
        }
    }




    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        //playerTexture.dispose();
        mapTexture.dispose();
        coinManager.dispose();
        silverKeyManager.dispose();
        goldenKeyManager.dispose();
        whitePixel.dispose();
        font.dispose();
    }
}
