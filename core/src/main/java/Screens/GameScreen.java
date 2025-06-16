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

import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
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

    private boolean paused = false;
    private static final float PLAYER_SPEED = 50f;

    private boolean gameWon = false;
    private long winTime = 0;
    private List<Entity> owls;




    //  Coruja
    private Texture owlTexture;
    private Entity owl;
    private long lastDirectionChangeTime;


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
        // ⚠️ Corrigir velocidade ao voltar da casa
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        if (vel != null) {
            vel.speed = PLAYER_SPEED;  // Usa o valor definido no topo (100f)
        }


        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        if (sprite != null) {
            sprite.scale = 1f; // Ou o valor que usavas antes
        }


        // COINS
        coinManager = new CoinManager();
        coinTexture = new Texture(Gdx.files.internal("coin.png"));
        coinIcon = new Texture(Gdx.files.internal("coin.png"));
        if (!inventory.hasItem(ItemType.COIN)) {
            coinManager.addCoin(new Coin(700, 100), this);
            coinManager.addCoin(new Coin(400, 200), this);
        }

// SILVER KEY
        silverKeyManager = new SilverKeyManager();
        silverKeyTexture = new Texture(Gdx.files.internal("House_Key.png"));
        silverKeyIcon = new Texture(Gdx.files.internal("House_Key.png"));
        if (!inventory.hasItem(ItemType.SILVER_KEY)) {
            silverKeyManager.addSilverKey(new SilverKey(450, 150), this);
        }

// GOLDEN KEY
        goldenKeyManager = new GoldenKeyManager();
        goldenKeyTexture = new Texture(Gdx.files.internal("Castle_Key.png"));
        goldenKeyIcon = new Texture(Gdx.files.internal("Castle_Key.png"));
        if (!inventory.hasItem(ItemType.GOLDEN_KEY)) {
            goldenKeyManager.addGoldenKey(new GoldenKey(650, 150), this);
        }

// LOCKET (caso não seja apanhado noutro ecrã, podes deixar ou remover aqui)
        locketManager = new LocketManager();
        locketTexture = new Texture(Gdx.files.internal("locket.png"));
        locketIcon = new Texture(Gdx.files.internal("locket.png"));

// WAND (caso seja apanhada noutro ecrã, apenas carrega a textura e ícone)
        wandManager = new WandManager();
        wandTexture = new Texture(Gdx.files.internal("Wand.png"));
        wandIcon = new Texture(Gdx.files.internal("Wand.png"));

        owlTexture = new Texture(Gdx.files.internal("owl.png"));
        owls = new ArrayList<>();


        for (int i = 0; i < 5; i++) { // número de corujas que quiseres
            float x = (float)(Math.random() * mapWidth);
            float y = (float)(Math.random() * mapHeight);
            Entity owl = EntityFactory.createOwl(x, y, owlTexture);
            owls.add(owl);
            entityManager.addEntity(owl);
        }

    }


    @Override
    public void render(float delta) {
        try {

            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                paused = !paused;
            }

            handleInput();
            movementSystem.update(entityManager.getEntities(), delta, mapWidth, mapHeight);
            animationSystem.update(entityManager.getEntities(), delta);


            PositionComponent pos = player.getComponent(PositionComponent.class);

            coinManager.updateAndNotifyCoins(pos.x, pos.y, inventory);
            silverKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            goldenKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
            locketManager.updateAndNotifyLockets(pos.x, pos.y, inventory);
            wandManager.updateAndNotifyWands(pos.x, pos.y, inventory);

            if (!paused) {
                handleInput();
                movementSystem.update(entityManager.getEntities(), delta, mapWidth, mapHeight);
                player.getComponent(AnimationComponent.class).update(delta);

                for (Entity owl : owls) {
                    VelocityComponent owlVel = owl.getComponent(VelocityComponent.class);
                    PositionComponent owlPos = owl.getComponent(PositionComponent.class);

                }


                if (TimeUtils.timeSinceMillis(lastDirectionChangeTime) > 1000) {
                    float[] speeds = {-50, 0, 50};

                    for (Entity owl : owls) {
                        VelocityComponent owlVel = owl.getComponent(VelocityComponent.class);
                        PositionComponent owlPos = owl.getComponent(PositionComponent.class);

                        owlVel.vx = speeds[(int) (Math.random() * speeds.length)];
                        owlVel.vy = speeds[(int) (Math.random() * speeds.length)];

                        if (owlPos.x < 0 || owlPos.x > mapWidth - TILE_SIZE) owlVel.vx *= -1;
                        if (owlPos.y < 0 || owlPos.y > mapHeight - TILE_SIZE) owlVel.vy *= -1;
                    }

                    lastDirectionChangeTime = TimeUtils.millis();
                }



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

            } // Desenhar mapa e entidades
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

            // 🟢 Ponto de vitória do jogo (tile 20,5)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.LIME);
            shapeRenderer.circle(22 * TILE_SIZE + 16, 3 * TILE_SIZE + 16, 6);
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

            if (paused) {
                font.getData().setScale(2.5f);
                font.setColor(Color.RED);
                font.draw(batch, "Jogo Pausado", camera.position.x - 100, camera.position.y);
                font.getData().setScale(1.0f);
                font.setColor(Color.WHITE);
            }

            if (gameWon) {
                font.getData().setScale(2f);
                font.setColor(Color.YELLOW);
                font.draw(batch, "🎉 YOU WIN! GAME OVER 🎉", camera.position.x - 140, camera.position.y + 40);
                font.setColor(Color.WHITE);
                font.getData().setScale(1f);

                // Fecha o jogo após 3 segundos
                if (TimeUtils.timeSinceMillis(winTime) > 3000) {
                    Gdx.app.exit();
                }
            }





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

            if (tileX == 22 && tileY == 3 &&
                inventory.hasItem(ItemType.COIN) &&
                inventory.hasItem(ItemType.SILVER_KEY) &&
                inventory.hasItem(ItemType.GOLDEN_KEY) &&
                inventory.hasItem(ItemType.LOCKET) &&
                inventory.hasItem(ItemType.WAND)) {

                if (!gameWon) {
                    gameWon = true;
                    winTime = TimeUtils.millis();
                    System.out.println("🎉 YOU WIN! PARABÉNS! 🎉");
                }
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
        owlTexture.dispose();
    }
}
