package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bd2r.game.CollisionMap;
import com.bd2r.game.MainGame;
import com.bd2r.game.MapLoader;
import com.bd2r.game.Observer.ItemType;
import com.bd2r.game.Observer.Wand;
import com.bd2r.game.Observer.WandManager;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.components.*;
import com.bd2r.game.ecs.systems.AnimationSystem;
import com.bd2r.game.ecs.systems.MovementSystem;
import com.bd2r.game.ecs.systems.RenderSystem;
import com.bd2r.game.pathfinder.AStarPathfinder;
import com.bd2r.game.pathfinder.Node;
import com.bd2r.game.Inventory;


import java.util.List;

public class HogwartsScreen implements Screen {

    private final MainGame game;
    private final Entity player;
    private final Texture playerTexture;

    private final EntityManager entityManager = new EntityManager();
    private final MovementSystem movementSystem = new MovementSystem();
    private final AnimationSystem animationSystem = new AnimationSystem();
    private final RenderSystem renderSystem = new RenderSystem();

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture mapTexture;
    private int mapWidth, mapHeight;
    private CollisionMap collisionMap;

    private Texture whitePixel;
    private BitmapFont font;
    private Texture coinIcon;
    private Texture silverKeyIcon;
    private Texture goldenKeyIcon;
    private Texture locketIcon;
    private Texture wandIcon;

    private WandManager wandManager;
    private Texture wandTexture;
    private Wand wand;


    private static final int TILE_SIZE = 32;

    public HogwartsScreen(MainGame game, Entity player, Texture playerTexture) {
        this.game = game;
        this.player = player;
        this.playerTexture = playerTexture;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        font = new BitmapFont();
        font.getData().setScale(1f);

        coinIcon = new Texture(Gdx.files.internal("coin.png"));
        silverKeyIcon = new Texture(Gdx.files.internal("House_Key.png"));
        goldenKeyIcon = new Texture(Gdx.files.internal("Castle_Key.png"));
        locketIcon = new Texture(Gdx.files.internal("locket.png"));
        wandIcon = new Texture(Gdx.files.internal("Wand.png"));


        collisionMap = new CollisionMap("collisionshogwarts.txt");
        mapTexture = new Texture(Gdx.files.internal("hogwarts.jpg"));
        mapWidth = mapTexture.getWidth();
        mapHeight = mapTexture.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        if (sprite != null) sprite.scale = 2f;

        // --- Inicializar varinha em Hogwarts ---
        wandManager = new WandManager();
        wandTexture = new Texture(Gdx.files.internal("Wand.png"));
        wandIcon = new Texture(Gdx.files.internal("Wand.png"));
        wandManager.addWand(new Wand(600, 500), this); // <-- ajusta posição se quiseres


        PositionComponent pos = player.getComponent(PositionComponent.class);
        if (pos != null) {
            pos.x = 150;
            pos.y = 100;
        }

        if (player.getComponent(VelocityComponent.class) == null)
            player.addComponent(new VelocityComponent(0, 0, 100));

        if (player.getComponent(PathComponent.class) == null)
            player.addComponent(new PathComponent());

        AnimationComponent anim = player.getComponent(AnimationComponent.class);
        if (anim != null && sprite != null) {
            anim.setDirection("down");
            anim.update(0); // força a frame inicial
            sprite.region = anim.getCurrentFrame();
        }

        entityManager.addEntity(player);
    }

    @Override
    public void render(float delta) {
        handleInput();

        movementSystem.update(entityManager.getEntities(), delta, mapWidth, mapHeight);
        animationSystem.update(entityManager.getEntities(), delta);

        PositionComponent pos = player.getComponent(PositionComponent.class);
        wandManager.updateAndNotifyWands(pos.x, pos.y, game.getInventory());
        checkTriggers(pos.x, pos.y);


        if (pos != null) {
            camera.position.set(pos.x + 16, pos.y + 16, 0);
        }

        clampCameraPosition();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.justTouched()) {
            try {
                float worldX = camera.position.x - camera.viewportWidth / 2 + Gdx.input.getX();
                float worldY = camera.position.y + camera.viewportHeight / 2 - Gdx.input.getY();

                int tileX = (int) (worldX / TILE_SIZE);
                int tileY = (int) (worldY / TILE_SIZE);

                int startX = (int) (pos.x / TILE_SIZE);
                int startY = (int) (pos.y / TILE_SIZE);

                AStarPathfinder pathfinder = new AStarPathfinder(MapLoader.loadMap("collisionshogwarts.txt"));
                List<Node> path = pathfinder.findPath(startX, startY, tileX, tileY);

                if (path != null && !path.isEmpty() && path.get(0).x == startX && path.get(0).y == startY) {
                    path.remove(0);
                }

                if (path != null && !path.isEmpty()) {
                    PathComponent pathComp = player.getComponent(PathComponent.class);
                    pathComp.setPath(path);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        batch.begin();
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());
        wandManager.render(batch, wandTexture, delta);


        // --- INVENTÁRIO VISUAL ---
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


// Fundo translúcido
        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(whitePixel, inventoryX - 16, inventoryY - 140, 180, 150);
        batch.setColor(Color.WHITE);


// Aceder ao inventário através do game
        Inventory inventory = game.getInventory();


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

    }

    private void handleInput() {
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        AnimationComponent anim = player.getComponent(AnimationComponent.class);
        PathComponent path = player.getComponent(PathComponent.class);

        if (vel == null || anim == null) return;

        // ⚠️ Cancelar caminho ao usar teclas
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vel.vx = 0;
            vel.vy = 100;
            anim.setDirection("up");
            if (path != null) path.path.clear();
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vel.vx = 0;
            vel.vy = -100;
            anim.setDirection("down");
            if (path != null) path.path.clear();
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            vel.vx = -100;
            vel.vy = 0;
            anim.setDirection("left");
            if (path != null) path.path.clear();
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vel.vx = 100;
            vel.vy = 0;
            anim.setDirection("right");
            if (path != null) path.path.clear();
        } else {
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

        System.out.println("tileX = " + tileX + ", tileY = " + tileY); // para debug

        PathComponent pathComp = player.getComponent(PathComponent.class);

        if (pathComp != null && pathComp.path.isEmpty()) {
            Inventory inventory = game.getInventory();

            // 🔐 SAIR de Hogwarts com a varinha
            if (tileX == 4 && tileY == 3 && inventory.getItemCount(ItemType.WAND) > 0) {
                if (batch.isDrawing()) batch.end();

                Gdx.app.postRunnable(() -> {
                    PositionComponent pos = player.getComponent(PositionComponent.class);
                    if (pos != null) {
                        pos.x = 7 * TILE_SIZE;  // 224
                        pos.y = 41 * TILE_SIZE; // 1312
                    }
                    game.setScreen(new GameScreen(game, player, playerTexture));
                    dispose();
                });
            }
        }
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
