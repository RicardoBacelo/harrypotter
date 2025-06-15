package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.bd2r.game.CollisionMap;
import com.bd2r.game.Inventory;
import com.bd2r.game.MainGame;
import com.bd2r.game.Observer.ItemType;
import com.bd2r.game.MapLoader;
import com.bd2r.game.Observer.Locket;
import com.bd2r.game.Observer.LocketManager;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.components.*;
import com.bd2r.game.ecs.systems.RenderSystem;
import com.bd2r.game.pathfinder.AStarPathfinder;
import com.bd2r.game.pathfinder.Node;

import java.util.List;

public class HagridHouseScreen implements Screen {

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

    private LocketManager locketManager;
    private Texture locketTexture;
    private Locket locket;

    private final Inventory inventory;
    private Texture whitePixel;


    // Ícones do inventário
    private Texture coinIcon;
    private Texture silverKeyIcon;
    private Texture goldenKeyIcon;
    private Texture locketIcon;
    private Texture wandIcon;
    private BitmapFont font;




    public HagridHouseScreen(MainGame game, Entity player, Texture playerTexture, Inventory inventory) {
        this.game = game;
        this.player = player;
        this.playerTexture = playerTexture;
        this.inventory = inventory;
        this.entityManager = new EntityManager();
        this.renderSystem = new RenderSystem();
    }


    @Override
    public void show() {
        batch = new SpriteBatch();
        setupInput();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();


        // 🔸 Inicializar fonte para o inventário
        font = new BitmapFont();
        font.getData().setScale(1f);

        // 🔸 Carregar os ícones do inventário
        coinIcon = new Texture(Gdx.files.internal("coin.png"));
        silverKeyIcon = new Texture(Gdx.files.internal("House_Key.png"));
        goldenKeyIcon = new Texture(Gdx.files.internal("Castle_Key.png"));
        locketIcon = new Texture(Gdx.files.internal("locket.png"));
        wandIcon = new Texture(Gdx.files.internal("Wand.png"));

        // 🔸 Carregar mapa visual e colisões
        mapTexture = new Texture(Gdx.files.internal("casa.jpg"));
        mapWidth = mapTexture.getWidth();
        mapHeight = mapTexture.getHeight();
        collisionMap = new CollisionMap("casahagrid.txt");

        // 🔸 Inicializar câmara
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 🔸 Preparar sprite do jogador
        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        if (sprite != null) {
            sprite.scale = 2f;
        }

        // 🔸 Posição inicial do jogador
        PositionComponent pos = player.getComponent(PositionComponent.class);
        if (pos != null) {
            pos.x = 500;
            pos.y = 100;
        }

        // 🔸 Velocidade
        if (player.getComponent(VelocityComponent.class) == null) {
            player.addComponent(new VelocityComponent(0, 0, 100));
        }

        // 🔸 Animação inicial
        AnimationComponent anim = player.getComponent(AnimationComponent.class);
        if (anim != null && sprite != null) {
            anim.setDirection("down");
            sprite.region = anim.getCurrentFrame();
        }

        // 🔸 Inicializar o Locket e adicioná-lo ao gestor
        locketManager = new LocketManager();
        locketTexture = new Texture(Gdx.files.internal("locket.png"));
        locket = new Locket(680, 710); // Posição inicial do medalhão
        locketManager.addLocket(locket, this);

        // 🔸 Adicionar o jogador ao sistema de entidades
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

        // ✅ ATUALIZA os lockets (recolha do medalhão)
        if (pos != null && vel != null) {
            locketManager.updateAndNotifyLockets(pos.x, pos.y, inventory);
        }

        if (pos != null && vel != null && sprite != null) {
            PathComponent path = player.getComponent(PathComponent.class);
            if (path != null && !path.path.isEmpty()) {
                Node next = path.path.peek();
                float targetX = next.x * 32;
                float targetY = next.y * 32;

                float speed = vel.speed * delta;
                float dx = targetX - pos.x;
                float dy = targetY - pos.y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < speed) {
                    pos.x = targetX;
                    pos.y = targetY;
                    path.path.remove();
                } else {
                    vel.vx = (dx / dist) * vel.speed;
                    vel.vy = (dy / dist) * vel.speed;

                    if (Math.abs(dx) > Math.abs(dy)) {
                        anim.setDirection(dx > 0 ? "right" : "left");
                    } else {
                        anim.setDirection(dy > 0 ? "up" : "down");
                    }
                }
            } else {
                vel.vx = 0;
                vel.vy = 0;
            }

            float nextX = pos.x + vel.vx * delta;
            float nextY = pos.y + vel.vy * delta;

            float spriteWidth = 32 * sprite.scale;
            float spriteHeight = 32 * sprite.scale;

            if (!collisionMap.isBlocked(nextX, pos.y, spriteWidth, spriteHeight)) {
                pos.x = nextX;
            }
            if (!collisionMap.isBlocked(pos.x, nextY, spriteWidth, spriteHeight)) {
                pos.y = nextY;
            }

            pos.x = Math.max(0, Math.min(pos.x, mapWidth - spriteWidth));
            pos.y = Math.max(0, Math.min(pos.y, mapHeight - spriteHeight));
        }

        if (pos != null) {
            camera.position.set(pos.x + 32, pos.y + 32, 0);
        }

        clampCameraPosition();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());

        // ✅ Renderiza o locket (animação do objeto)
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
    }


    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
                    int tileX = (int) (worldCoords.x / 32);
                    int tileY = (int) (worldCoords.y / 32);

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

        int startX = (int) (pos.x / 32);
        int startY = (int) (pos.y / 32);

        System.out.println("Início: " + startX + "," + startY + " | Destino: " + targetX + "," + targetY);


        try {
            AStarPathfinder pathfinder = new AStarPathfinder(MapLoader.loadMap("casahagrid.txt"));
            List<Node> path = pathfinder.findPath(startX, startY, targetX, targetY);

// ✅ Remover primeiro passo se for a posição atual do jogador
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

    private void handleInput() {
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        AnimationComponent anim = player.getComponent(AnimationComponent.class);

        if (vel == null || anim == null) return;

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vel.vy = 100;
            vel.vx = 0;
            anim.setDirection("up");
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vel.vy = -100;
            vel.vx = 0;
            anim.setDirection("down");
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            vel.vx = -100;
            vel.vy = 0;
            anim.setDirection("left");
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            vel.vx = 100;
            vel.vy = 0;
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
