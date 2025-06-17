package Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.bd2r.game.*;
import com.bd2r.game.Observer.*;
import com.bd2r.game.ecs.entity.Entity;
import com.bd2r.game.ecs.entity.EntityManager;
import com.bd2r.game.ecs.components.*;
import com.bd2r.game.ecs.systems.CameraSystem;
import com.bd2r.game.ecs.systems.RenderSystem;
import com.bd2r.game.factory.EntityFactory;
import com.bd2r.game.pathfinder.*;
import java.util.ArrayList;
import java.util.List;

public class HagridHouseScreen implements Screen {

    private final MainGame game;
    private final Entity player;
    private final Texture playerTexture;
    private final Inventory inventory;
    private final List<Entity> spiders = new ArrayList<>();
    private float spiderTimer = 0f;
    private float spiderChangeInterval = 2f; // muda de direção a cada 2 segundos
    private final EntityManager entityManager = new EntityManager();
    private final RenderSystem renderSystem = new RenderSystem();

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture mapTexture;
    private int mapWidth, mapHeight;
    private CollisionMap collisionMap;

    private Texture whitePixel;
    private BitmapFont font;

    private LocketManager locketManager;
    private Texture locketTexture;

    private static final int TILE_SIZE = 32;


    private Texture coinIcon, silverKeyIcon, goldenKeyIcon, locketIcon, wandIcon;

    public HagridHouseScreen(MainGame game, Entity player, Texture playerTexture, Inventory inventory) {
        this.game = game;
        this.player = player;
        this.playerTexture = playerTexture;
        this.inventory = inventory;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        Texture spiderTexture = new Texture(Gdx.files.internal("spider.png"));

        for (int i = 0; i < 3; i++) {
            float x = 550 + i * 40;
            float y = 220 + i * 30;
            Entity spider = EntityFactory.createSpider(x, y, spiderTexture);
            spiders.add(spider);
            entityManager.addEntity(spider);
        }


        font = new BitmapFont();
        font.getData().setScale(1f);
        whitePixel = createWhitePixel();

        coinIcon = new Texture(Gdx.files.internal("coin.png"));
        silverKeyIcon = new Texture(Gdx.files.internal("House_Key.png"));
        goldenKeyIcon = new Texture(Gdx.files.internal("Castle_Key.png"));
        locketIcon = new Texture(Gdx.files.internal("locket.png"));
        wandIcon = new Texture(Gdx.files.internal("Wand.png"));

        mapTexture = new Texture(Gdx.files.internal("casa.jpg"));
        mapWidth = mapTexture.getWidth();
        mapHeight = mapTexture.getHeight();
        collisionMap = new CollisionMap("casahagrid.txt");

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        setupPlayer();
        setupInput();


        locketManager = new LocketManager();
        locketTexture = new Texture(Gdx.files.internal("locket.png"));
        locketManager.addLocket(new Locket(680, 710), this);

        entityManager.addEntity(player);
    }

    private Texture createWhitePixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private void setupPlayer() {
        PositionComponent pos = player.getComponent(PositionComponent.class);
        if (pos != null) {
            pos.x = 500;
            pos.y = 100;
        }

        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        if (sprite != null) {
            sprite.scale = 2f;
        }

        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        if (vel == null) {
            player.addComponent(new VelocityComponent(0, 0, 150f)); // nova velocidade
        } else {
            vel.speed = 150f; // força nova velocidade mesmo que já exista
        }


        AnimationComponent anim = player.getComponent(AnimationComponent.class);
        if (anim != null && sprite != null) {
            anim.setDirection("down");
            sprite.region = anim.getCurrentFrame();
        }
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / TILE_SIZE);
                    int tileY = (int) (worldCoords.y / TILE_SIZE);
                    movePlayerTo(tileX, tileY);
                    return true;
                }
                return false;
            }
        });
    }

    private void movePlayerTo(int targetX, int targetY) {
        PositionComponent pos = player.getComponent(PositionComponent.class);
        if (pos == null) return;

        int startX = (int) (pos.x / TILE_SIZE);
        int startY = (int) (pos.y / TILE_SIZE);

        try {
            AStarPathfinder pathfinder = new AStarPathfinder(MapLoader.loadMap("casahagrid.txt"));
            List<Node> path = pathfinder.findPath(startX, startY, targetX, targetY);

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

    @Override
    public void render(float delta) {
        handleInput();

        spiderTimer += delta;
        if (spiderTimer >= spiderChangeInterval) {
            spiderTimer = 0f;
            for (Entity s : spiders) {
                VelocityComponent vel = s.getComponent(VelocityComponent.class);
                if (vel != null) {
                    int dir = MathUtils.random(3);
                    switch (dir) {
                        case 0: vel.vx = 0; vel.vy = vel.speed; break;
                        case 1: vel.vx = 0; vel.vy = -vel.speed; break;
                        case 2: vel.vx = -vel.speed; vel.vy = 0; break;
                        case 3: vel.vx = vel.speed; vel.vy = 0; break;
                    }
                }
            }




        }

        for (Entity spider : spiders) {
            PositionComponent pos = spider.getComponent(PositionComponent.class);
            VelocityComponent vel = spider.getComponent(VelocityComponent.class);
            SpriteComponent sprite = spider.getComponent(SpriteComponent.class);
            AnimationComponent anim = spider.getComponent(AnimationComponent.class);

            if (anim != null && sprite != null) {
                anim.update(delta);
                sprite.region = anim.getCurrentFrame();
            }

            if (pos != null && vel != null && sprite != null) {
                float nextX = pos.x + vel.vx * delta;
                float nextY = pos.y + vel.vy * delta;
                float width = TILE_SIZE * sprite.scale;
                float height = TILE_SIZE * sprite.scale;

                if (!collisionMap.isBlocked(nextX, pos.y, width, height)) pos.x = nextX;
                if (!collisionMap.isBlocked(pos.x, nextY, width, height)) pos.y = nextY;
            }
        }



        PositionComponent pos = player.getComponent(PositionComponent.class);
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        AnimationComponent anim = player.getComponent(AnimationComponent.class);

        if (anim != null && sprite != null) {
            anim.update(delta);
            sprite.region = anim.getCurrentFrame();
        }

        if (pos != null && vel != null && sprite != null) {
            locketManager.updateAndNotifyLockets(pos.x, pos.y, inventory);
            checkTriggers(pos.x, pos.y);

            PathComponent path = player.getComponent(PathComponent.class);
            if (path != null && !path.path.isEmpty()) {
                Node next = path.path.peek();
                float targetX = next.x * TILE_SIZE;
                float targetY = next.y * TILE_SIZE;
                float dx = targetX - pos.x;
                float dy = targetY - pos.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float speed = vel.speed * delta;

                if (dist < speed) {
                    pos.x = targetX;
                    pos.y = targetY;
                    path.path.remove();
                } else {
                    vel.vx = (dx / dist) * vel.speed;
                    vel.vy = (dy / dist) * vel.speed;

                    if (Math.abs(dx) > Math.abs(dy))
                        anim.setDirection(dx > 0 ? "right" : "left");
                    else
                        anim.setDirection(dy > 0 ? "up" : "down");
                }
            } else {
                vel.vx = 0;
                vel.vy = 0;
            }

            float nextX = pos.x + vel.vx * delta;
            float nextY = pos.y + vel.vy * delta;
            float width = TILE_SIZE * sprite.scale;
            float height = TILE_SIZE * sprite.scale;

            if (!collisionMap.isBlocked(nextX, pos.y, width, height)) pos.x = nextX;
            if (!collisionMap.isBlocked(pos.x, nextY, width, height)) pos.y = nextY;

            pos.x = Math.max(0, Math.min(pos.x, mapWidth - width));
            pos.y = Math.max(0, Math.min(pos.y, mapHeight - height));

            camera.position.set(pos.x + TILE_SIZE, pos.y + TILE_SIZE, 0);
        }

        CameraSystem.clampCameraPosition(mapWidth, mapHeight, camera);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());
        locketManager.render(batch, locketTexture, delta);
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


        // Só mostra ponto vermelho se já tiver o medalhão
        if (inventory.getItemCount(ItemType.LOCKET) > 0) {
            ShapeRenderer shapeRenderer = new ShapeRenderer();
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.RED);
            // Ponto vermelho na tile de saída (ajustada para evitar loop)
            shapeRenderer.circle(15 * TILE_SIZE + TILE_SIZE / 2f, TILE_SIZE + TILE_SIZE / 2f, 6);// centro do mapa
            shapeRenderer.end();
            shapeRenderer.dispose();
        }
    }

    private void checkTriggers(float x, float y) {
        int tileX = (int) (x / TILE_SIZE);
        int tileY = (int) (y / TILE_SIZE);

        PathComponent pathComp = player.getComponent(PathComponent.class);

        if (pathComp != null && pathComp.path.isEmpty()) {
            if (tileX == 15 && tileY == 1 && inventory.getItemCount(ItemType.LOCKET) > 0) {
                if (batch.isDrawing()) batch.end();

                Gdx.app.postRunnable(() -> {
                    PositionComponent pos = player.getComponent(PositionComponent.class);
                    SpriteComponent sprite = player.getComponent(SpriteComponent.class);
                    if (pos != null) {
                        pos.x = 8 * TILE_SIZE;
                        pos.y = 34 * TILE_SIZE;
                    }
                    if (sprite != null) {
                        sprite.scale = 1f;
                    }
                    game.setScreen(new GameScreen(game, player, playerTexture));
                    dispose();
                });
            }
        }
    }

    private void handleInput() {
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        AnimationComponent anim = player.getComponent(AnimationComponent.class);
        if (vel == null || anim == null) return;

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vel.vy = 100; vel.vx = 0; anim.setDirection("up");
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vel.vy = -100; vel.vx = 0; anim.setDirection("down");
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            vel.vx = -100; vel.vy = 0; anim.setDirection("left");
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vel.vx = 100; vel.vy = 0; anim.setDirection("right");
        } else {
            vel.vx = 0; vel.vy = 0;
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
        batch.dispose();
        mapTexture.dispose();
        whitePixel.dispose();
        font.dispose();
    }
}

