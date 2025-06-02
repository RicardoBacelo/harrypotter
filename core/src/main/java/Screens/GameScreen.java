package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import items.Coin;
import items.GoldenKey;
import items.SilverKey;
import observer.ItemType;
import observer.managers.CoinManager;
import observer.managers.GoldenKeyManager;
import observer.managers.SilverKeyManager;
import ui.Inventory;
import core.MainGame;
import World.map.MapLoader;
import items.Locket;
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

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen implements Screen {

    //Gestão das entidades e sistemas do jogo
    private final EntityManager entityManager = new EntityManager();
    private final MovementSystem movementSystem = new MovementSystem();
    private final RenderSystem renderSystem = new RenderSystem();

    //Recursos gráficos e texturas do jogo
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture playerTexture, mapTexture;

    //Jogador e respetivas animações de movimentos
    private Entity player;
    private TextureRegion[] walkUpFrames, walkDownFrames, walkLeftFrames, walkRightFrames;

    private OrthographicCamera camera;
    private static final int TILE_SIZE = 32;
    private int mapWidth, mapHeight;

    //Gestão de itens e texturas
    private CoinManager coinManager;
    private Texture coinTexture;
    private SilverKeyManager silverKeyManager;
    private Texture silverKeyTexture;
    private GoldenKeyManager goldenKeyManager;
    private Texture goldenKeyTexture;

    private final Inventory inventory;
    private Texture coinIcon, silverKeyIcon, goldenKeyIcon;
    private BitmapFont font;
    private Texture whitePixel;

    private final Map<Point, Screen> triggers = new HashMap<>(); //Triggers de troca de tela
    private final MainGame game;

    //Contrutor da classe GameScreen
    public GameScreen(MainGame game) {
        this.game = game;  // Store the passed game instance
        this.inventory = game.getInventory();

    }

    //Mostra os recursos do ecra de jogo
    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        //Fonts
        font = new BitmapFont();
        font.getData().setScale(1.0f);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Carrega mapa
        mapTexture = new Texture(Gdx.files.internal("mundo.png"));
        mapWidth = mapTexture.getWidth();
        mapHeight = mapTexture.getHeight();

        // 1) Registo dos triggers em tiles (xTile, yTile → new Screen)
        triggers.put(new Point(16, 5), new HagridHouseScreen(game));
        //Adicionar os triggers necessários

        playerTexture = new Texture(Gdx.files.internal("hero1.png"));

        walkUpFrames = new TextureRegion[3];
        walkDownFrames = new TextureRegion[3];
        walkLeftFrames = new TextureRegion[3];
        walkRightFrames = new TextureRegion[3];

// Cada linha tem 32 de altura, cada coluna 32 de largura
        for (int i = 0; i < 3; i++) {
            walkDownFrames[i] = new TextureRegion(playerTexture, i * 32, 0, 32, 32);  // linha 1
            walkLeftFrames[i] = new TextureRegion(playerTexture, i * 32, 32, 32, 32);  // linha 2
            walkRightFrames[i] = new TextureRegion(playerTexture, i * 32, 64, 32, 32);  // linha 3
            walkUpFrames[i] = new TextureRegion(playerTexture, i * 32, 96, 32, 32);  // linha 4
        }

        //Adiciona moedas no mapa
        coinManager = new CoinManager();
        coinTexture = new Texture(Gdx.files.internal("coin.png"));
        coinManager.addCoin(new Coin(500, 100), this);
        coinManager.addCoin(new Coin(400, 150), this);

        //Adiciona chave prateada no mapa
        silverKeyManager = new SilverKeyManager();
        silverKeyTexture = new Texture(Gdx.files.internal("House_Key.png"));
        silverKeyManager.addSilverKey(new SilverKey(500, 150), this);

        //Adiciona chave dourada no mapa
        goldenKeyManager = new GoldenKeyManager();
        goldenKeyTexture = new Texture(Gdx.files.internal("Castle_Key.png"));
        goldenKeyManager.addGoldenKey(new GoldenKey(750, 150), this);

        //Indica asset para as texturas do itens
        coinIcon = new Texture(Gdx.files.internal("coin.png"));
        silverKeyIcon = new Texture(Gdx.files.internal("House_Key.png"));
        goldenKeyIcon = new Texture(Gdx.files.internal("Castle_Key.png"));

        player = EntityFactory.createPlayer(485, 60, walkDownFrames[1]);
        player.addComponent(new AnimationComponent(walkUpFrames, 0.2f));
        entityManager.addEntity(player);
    }

    @Override
    public void render(float delta) {
        try {

            handleInput();
            movementSystem.update(entityManager.getEntities(), delta, mapWidth, mapHeight);

            player.getComponent(AnimationComponent.class).update(delta);
            PositionComponent pos = player.getComponent(PositionComponent.class);

            //Inventory inventory = this.inventory;
            coinManager.updateAndNotifyCoins(pos.x, pos.y, inventory);
            silverKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            goldenKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);

            // Center camera on player
            camera.position.set(pos.x + 16, pos.y + 16, 0);
            clampCameraPosition();
            camera.update();

            // Clear screen and draw
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.3f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            // CLICK TO MOVE
            if (Gdx.input.justTouched()) {
                try {
                    // Convert screen to world coords
                    float worldX = camera.position.x - camera.viewportWidth / 2 + Gdx.input.getX();
                    float worldY = camera.position.y + camera.viewportHeight / 2 - Gdx.input.getY();

                    // Convert to tile coordinates
                    int tileX = (int) (worldX / TILE_SIZE);
                    int tileY = (int) (worldY / TILE_SIZE);

                    int startX = (int) (pos.x / TILE_SIZE);
                    int startY = (int) (pos.y / TILE_SIZE);

                    System.out.println("🖱️ Clicked tile: " + tileX + "," + tileY);
                    System.out.println("👣 Player at tile: " + startX + "," + startY);

                    AStarPathfinder pathfinder = new AStarPathfinder(MapLoader.loadMap("mapa.txt")); // path corrected
                    List<Node> path = pathfinder.findPath(startX, startY, tileX, tileY);

                    if (path != null && !path.isEmpty()) {
                        System.out.println("✅ Path found! " + path.size() + " steps.");
                        // Check if PathComponent already exists
                        PathComponent pathComp = player.getComponent(PathComponent.class);
                        if (pathComp == null) {
                            // If it doesn't exist, create new one
                            pathComp = new PathComponent();
                            player.addComponent(pathComp);
                        }
                        // Update the path
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
            // Draw game world elements first
            batch.draw(mapTexture, 0, 0);
            renderSystem.render(batch, entityManager.getEntities());
            coinManager.updateAndNotifyCoins(pos.x, pos.y, inventory);
            coinManager.render(batch, coinTexture, delta);
            silverKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            silverKeyManager.render(batch, silverKeyTexture, delta);
            goldenKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            goldenKeyManager.render(batch, goldenKeyTexture, delta);

// End the world space batch and start a new one for UI
            batch.end();

// Start new batch for UI elements that follow the camera
            batch.begin();
// Reset the projection matrix to screen coordinates
            batch.setProjectionMatrix(camera.combined);

// Calculate inventory position relative to camera
            float inventoryX = camera.position.x + (camera.viewportWidth / 2) - 100;
            float inventoryY = camera.position.y - (camera.viewportHeight / 2) + 100;
            int iconSize = 24;
            float paddingY = 4f;

            // 1) Moeda
            batch.draw(coinIcon,inventoryX,inventoryY-iconSize, iconSize, iconSize);
            font.draw(batch,"x "+inventory.getItemCount(ItemType.COIN),
                inventoryX+iconSize+paddingY,
                inventoryY-iconSize/2f+6);

// 2) Chave prata
            batch.draw(silverKeyIcon, inventoryX,inventoryY-iconSize*2-8, iconSize, iconSize);
            font.draw(batch,"x "+inventory.getItemCount(ItemType.SILVER_KEY),
                inventoryX+iconSize+paddingY,
                inventoryY-iconSize*1.5f-8+6);

// 3) Chave dourada
            batch.draw(goldenKeyIcon, inventoryX,inventoryY-iconSize*3-16, iconSize, iconSize);
            font.draw(batch,"x "+inventory.getItemCount(ItemType.GOLDEN_KEY),
                inventoryX+iconSize+paddingY,
                inventoryY-iconSize*2.5f-16+6);

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
        // Convert world coordinates to tile coordinates
        int tileX = (int) (x / TILE_SIZE);
        int tileY = (int) (y / TILE_SIZE);


        PathComponent pathComp = player.getComponent(PathComponent.class);
        if (pathComp != null && pathComp.path.isEmpty() && triggers.containsKey(new Point(tileX, tileY))) {
            // End the current batch if it's active
            if (batch.isDrawing()) {
                batch.end();
            }

            // Create new screen
            Screen newScreen = new HagridHouseScreen(game);

            // Set the new screen first
            game.setScreen(newScreen);

            // Dispose after setting new screen
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
        coinIcon.dispose();
        silverKeyIcon.dispose();
        goldenKeyIcon.dispose();
        whitePixel.dispose();
        font.dispose();
    }
}
