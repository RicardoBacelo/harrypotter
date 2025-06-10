package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import ui.Inventory;
import observer.ItemType;
import observer.managers.CoinManager;
import observer.managers.GoldenKeyManager;
import core.MainGame;
import World.map.MapLoader;
import ecs.Entity;
import ecs.EntityManager;
import ecs.components.AnimationComponent;
import ecs.components.PathComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.systems.MovementSystem;
import ecs.systems.RenderSystem;
import factory.EntityFactory;
import World.pathfinding.AStarPathfinder;
import World.pathfinding.Node;
import observer.managers.SilverKeyManager;
import items.Coin;
import items.GoldenKey;
import items.SilverKey;

import java.util.List;

public class GameScreen implements Screen {

    private final EntityManager entityManager = new EntityManager();
    private final MovementSystem movementSystem = new MovementSystem();
    private final RenderSystem renderSystem = new RenderSystem();

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture playerTexture, mapTexture;

    private Entity player;
    private TextureRegion[] walkUpFrames, walkDownFrames, walkLeftFrames, walkRightFrames;

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

    // NOVO: variável para pausa
    private boolean paused = false;

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

        walkUpFrames = new TextureRegion[3];
        walkDownFrames = new TextureRegion[3];
        walkLeftFrames = new TextureRegion[3];
        walkRightFrames = new TextureRegion[3];

        for (int i = 0; i < 3; i++) {
            walkDownFrames[i] = new TextureRegion(playerTexture, i * 32, 0, 32, 32);
            walkLeftFrames[i] = new TextureRegion(playerTexture, i * 32, 32, 32, 32);
            walkRightFrames[i] = new TextureRegion(playerTexture, i * 32, 64, 32, 32);
            walkUpFrames[i] = new TextureRegion(playerTexture, i * 32, 96, 32, 32);
        }

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

        player = EntityFactory.createPlayer(485, 60, walkDownFrames[1]);
        player.addComponent(new AnimationComponent(walkUpFrames, 0.2f));
        entityManager.addEntity(player);
    }

    @Override
    public void render(float delta) {
        try {
            // NOVO: alternar pausa com tecla P
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                paused = !paused;
            }

            PositionComponent pos = player.getComponent(PositionComponent.class);

            if (!paused) {
                handleInput();
                movementSystem.update(entityManager.getEntities(), delta, mapWidth, mapHeight);
                player.getComponent(AnimationComponent.class).update(delta);

                coinManager.updateAndNotifyCoins(pos.x, pos.y, inventory);
                silverKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
                goldenKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);

                camera.position.set(pos.x + 16, pos.y + 16, 0);
                clampCameraPosition();
                camera.update();
            }

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

                    System.out.println("🖱️ Clicked tile: " + tileX + "," + tileY);
                    System.out.println("👣 Player at tile: " + startX + "," + startY);

                    AStarPathfinder pathfinder = new AStarPathfinder(MapLoader.loadMap("mapa.txt"));
                    List<Node> path = pathfinder.findPath(startX, startY, tileX, tileY);

                    if (path != null && !path.isEmpty()) {
                        System.out.println("✅ Path found! " + path.size() + " steps.");
                        PathComponent pathComp = player.getComponent(PathComponent.class);
                        if (pathComp == null) {
                            pathComp = new PathComponent();
                            player.addComponent(pathComp);
                        }
                        pathComp.setPath(path);
                    } else {
                        System.out.println("⚠️ No path found.");
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error on mouse click:");
                    e.printStackTrace();
                }
            }

            batch.begin();

            checkTriggers(pos.x, pos.y);

            batch.draw(mapTexture, 0, 0);
            renderSystem.render(batch, entityManager.getEntities());

            coinManager.updateAndNotifyCoins(pos.x, pos.y, inventory);
            coinManager.render(batch, coinTexture, delta);

            silverKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            silverKeyManager.render(batch, silverKeyTexture, delta);

            goldenKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            goldenKeyManager.render(batch, goldenKeyTexture, delta);

            batch.end();

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

            // NOVO: Mostrar mensagem de pausa
            if (paused) {
                font.getData().setScale(2.5f);
                font.setColor(Color.RED);
                font.draw(batch, "Jogo Pausado", camera.position.x - 100, camera.position.y);
                font.getData().setScale(1.0f);
                font.setColor(Color.WHITE);
            }

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
            anim.frames = walkLeftFrames;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vel.vx = 100;
            anim.frames = walkRightFrames;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vel.vy = 100;
            anim.frames = walkUpFrames;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vel.vy = -100;
            anim.frames = walkDownFrames;
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

        PathComponent pathComp = player.getComponent(PathComponent.class);
        if (pathComp != null && pathComp.path.isEmpty() && tileX == 16 && tileY == 5) {
            if (batch.isDrawing()) {
                batch.end();
            }
            Screen newScreen = new HagridHouseScreen(game);
            game.setScreen(newScreen);
            dispose();
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
        playerTexture.dispose();
        mapTexture.dispose();
        coinManager.dispose();
        silverKeyManager.dispose();
        goldenKeyManager.dispose();
        whitePixel.dispose();
        font.dispose();
    }
}
