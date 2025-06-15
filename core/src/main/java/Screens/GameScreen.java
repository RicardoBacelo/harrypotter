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
import com.bd2r.game.ecs.components.*;
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
    private Texture coinIcon;

    private SilverKeyManager silverKeyManager;
    private Texture silverKeyTexture;
    private Texture silverKeyIcon;

    private GoldenKeyManager goldenKeyManager;
    private Texture goldenKeyTexture;
    private Texture goldenKeyIcon;

    private LocketManager locketManager;
    private Texture locketTexture;
    private Texture locketIcon;

    private WandManager wandManager;
    private Texture wandTexture;
    private Texture wandIcon;

    private final Inventory inventory;
    private BitmapFont font;
    private Texture whitePixel;
    private final MainGame game;

    public GameScreen(MainGame game, Entity player, Texture playerTexture) {
        this.game = game;
        this.inventory = game.getInventory();
        this.player = player;
        this.playerTexture = playerTexture;
    }
    public GameScreen(MainGame game) {
        this(game, null, null);  // chama o outro construtor com valores nulos
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

        if (player == null || playerTexture == null) {
            playerTexture = new Texture(Gdx.files.internal("hero1.png"));
            player = EntityFactory.createPlayer(485, 60, playerTexture);
        }
        entityManager.addEntity(player);

        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        if (sprite != null) {
            sprite.scale = 1f; // Ou o valor que usavas antes
        }



        coinManager = new CoinManager();
        coinTexture = new Texture(Gdx.files.internal("coin.png"));
        coinManager.addCoin(new Coin(500, 100), this);
        coinManager.addCoin(new Coin(400, 150), this);
        coinIcon = new Texture(Gdx.files.internal("coin.png"));

        silverKeyManager = new SilverKeyManager();
        silverKeyTexture = new Texture(Gdx.files.internal("House_Key.png"));
        silverKeyManager.addSilverKey(new SilverKey(500, 150), this);
        silverKeyIcon = new Texture(Gdx.files.internal("House_Key.png"));

        goldenKeyManager = new GoldenKeyManager();
        goldenKeyTexture = new Texture(Gdx.files.internal("Castle_Key.png"));
        goldenKeyManager.addGoldenKey(new GoldenKey(750, 150), this);
        goldenKeyIcon = new Texture(Gdx.files.internal("Castle_Key.png"));

        locketManager = new LocketManager();
        locketTexture = new Texture(Gdx.files.internal("locket.png"));
        locketIcon = new Texture(Gdx.files.internal("locket.png"));

        wandManager = new WandManager();
        wandTexture = new Texture(Gdx.files.internal("Wand.png"));
        wandIcon = new Texture(Gdx.files.internal("Wand.png"));
    }

    @Override
    public void render(float delta) {
        try {

            handleInput();
            movementSystem.update(entityManager.getEntities(), delta, mapWidth, mapHeight);
            animationSystem.update(entityManager.getEntities(), delta);


            PositionComponent pos = player.getComponent(PositionComponent.class);
            coinManager.updateAndNotifyCoins(pos.x, pos.y, inventory);
            silverKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            goldenKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            locketManager.updateAndNotifyLockets(pos.x, pos.y, inventory);
            wandManager.updateAndNotifyWands(pos.x, pos.y, inventory);

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

                   // ✅ Remover primeiro passo se for o mesmo tile onde o jogador já está
                    if (path != null && !path.isEmpty() && path.get(0).x == startX && path.get(0).y == startY) {
                        path.remove(0);
                    }

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

            // Desenhar inventário com ícones
            batch.begin();
            batch.setProjectionMatrix(camera.combined);

            float inventoryX = camera.position.x + (camera.viewportWidth / 2) - 80;
            float inventoryY = camera.position.y - (camera.viewportHeight / 2) + 160;
            int iconSize = 24;
            float paddingY = 4f;

// Fundo do inventário
            batch.setColor(0f, 0f, 0f, 0.5f);
            batch.draw(whitePixel, inventoryX - 16, inventoryY - 160, 180, 180);
            batch.setColor(Color.WHITE);

// Título
            font.draw(batch, "Inventário", inventoryX, inventoryY + 15);

// Moeda
            batch.draw(coinIcon, inventoryX, inventoryY - iconSize, iconSize, iconSize);
            font.draw(batch, "x " + inventory.getItemCount(ItemType.COIN),
                inventoryX + iconSize + paddingY,
                inventoryY - iconSize / 2f + 6);

// Chave prata
            batch.draw(silverKeyIcon, inventoryX, inventoryY - iconSize * 2 - 8, iconSize, iconSize);
            font.draw(batch, "x " + inventory.getItemCount(ItemType.SILVER_KEY),
                inventoryX + iconSize + paddingY,
                inventoryY - iconSize * 1.5f - 8 + 6);

// Chave dourada
            batch.draw(goldenKeyIcon, inventoryX, inventoryY - iconSize * 3 - 16, iconSize, iconSize);
            font.draw(batch, "x " + inventory.getItemCount(ItemType.GOLDEN_KEY),
                inventoryX + iconSize + paddingY,
                inventoryY - iconSize * 2.5f - 16 + 6);

// Medalhão (locket)
            if (locketIcon != null) {
                batch.draw(locketIcon, inventoryX, inventoryY - iconSize * 4 - 24, iconSize, iconSize);
                font.draw(batch, "x " + inventory.getItemCount(ItemType.LOCKET),
                    inventoryX + iconSize + paddingY,
                    inventoryY - iconSize * 3.5f - 24 + 6);
            }

// Varinha
            batch.draw(wandIcon, inventoryX, inventoryY - iconSize * 5 - 32, iconSize, iconSize);
            font.draw(batch, "x " + inventory.getItemCount(ItemType.WAND),
                inventoryX + iconSize + paddingY,
                inventoryY - iconSize * 4.5f - 32 + 6);

            batch.end();


        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error in render", e);
        }
    }


    private void handleInput() {
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        AnimationComponent anim = player.getComponent(AnimationComponent.class);

        if (vel == null || anim == null) return;

        // Verifica se alguma tecla foi premida
        boolean keyPressed = false;
        vel.vx = 0;
        vel.vy = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            vel.vx = -100;
            anim.setDirection("left");
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vel.vx = 100;
            anim.setDirection("right");
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vel.vy = 100;
            anim.setDirection("up");
            keyPressed = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vel.vy = -100;
            anim.setDirection("down");
            keyPressed = true;
        }

        // ⚠️ Se nenhuma tecla estiver a ser premida, não alteres direção nem movimento
        if (!keyPressed) {
            vel.vx = 0;
            vel.vy = 0;
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
                game.setScreen(new HagridHouseScreen(game, player, playerTexture, inventory));

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
