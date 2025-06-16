package Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.bd2r.game.*;
import com.bd2r.game.Observer.*;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.components.*;
import com.bd2r.game.ecs.systems.RenderSystem;
import com.bd2r.game.pathfinder.*;

import java.util.List;

public class HagridHouseScreen implements Screen {

    // Referências ao jogo e estado
    private final MainGame game;
    private final Entity player;
    private final Texture playerTexture;
    private final Inventory inventory;

    // ECS e renderização
    private final EntityManager entityManager = new EntityManager();
    private final RenderSystem renderSystem = new RenderSystem();

    // Gráficos e câmara
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture mapTexture;
    private int mapWidth, mapHeight;
    private CollisionMap collisionMap;

    // UI
    private Texture whitePixel;
    private BitmapFont font;

    // Sistema de coleta
    private LocketManager locketManager;
    private Texture locketTexture;

    private static final int TILE_SIZE = 32;

    // Ícones do inventário
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

        // Inicializar fontes e elementos visuais
        font = new BitmapFont();
        font.getData().setScale(1f);
        whitePixel = createWhitePixel();

        // Carregar ícones
        coinIcon = new Texture(Gdx.files.internal("coin.png"));
        silverKeyIcon = new Texture(Gdx.files.internal("House_Key.png"));
        goldenKeyIcon = new Texture(Gdx.files.internal("Castle_Key.png"));
        locketIcon = new Texture(Gdx.files.internal("locket.png"));
        wandIcon = new Texture(Gdx.files.internal("Wand.png"));

        // Carregar mapa e colisões
        mapTexture = new Texture(Gdx.files.internal("casa.jpg"));
        mapWidth = mapTexture.getWidth();
        mapHeight = mapTexture.getHeight();
        collisionMap = new CollisionMap("casahagrid.txt");

        // Câmara
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        setupPlayer();
        setupInput();

        // Adicionar locket
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

    // Define posição, escala e velocidade do jogador
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
            player.addComponent(new VelocityComponent(0, 0, 150f));
        } else {
            vel.speed = 150f;
        }

        AnimationComponent anim = player.getComponent(AnimationComponent.class);
        if (anim != null && sprite != null) {
            anim.setDirection("down");
            sprite.region = anim.getCurrentFrame();
        }
    }

    // Regista toques no ecrã para iniciar movimento
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

    // Gera caminho com A* para onde o jogador tocou
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

        PositionComponent pos = player.getComponent(PositionComponent.class);
        VelocityComponent vel = player.getComponent(VelocityComponent.class);
        SpriteComponent sprite = player.getComponent(SpriteComponent.class);
        AnimationComponent anim = player.getComponent(AnimationComponent.class);

        // Atualiza animação do jogador
        if (anim != null && sprite != null) {
            anim.update(delta);
            sprite.region = anim.getCurrentFrame();
        }

        if (pos != null && vel != null && sprite != null) {
            // Verifica colisão com o locket
            locketManager.updateAndNotifyLockets(pos.x, pos.y, inventory);
            checkTriggers(pos.x, pos.y);

            // Movimento por pathfinding
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

            // Verifica colisões
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

        clampCameraPosition();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Renderiza mapa e entidades
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());
        locketManager.render(batch, locketTexture, delta);

        // UI do inventário
        float inventoryX = camera.position.x + (camera.viewportWidth / 2) - 80;
        float inventoryY = camera.position.y - (camera.viewportHeight / 2) + 160;
        int iconSize = 24;
        float paddingY = 4f;

        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(whitePixel, inventoryX - 16, inventoryY - 160, 180, 180);
        batch.setColor(Color.WHITE);

        font.draw(batch, "Inventário", inventoryX, inventoryY + 15);

        // Mostra contagem dos itens
        batch.draw(coinIcon, inventoryX, inventoryY - iconSize, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.COIN), inventoryX + iconSize + paddingY, inventoryY - iconSize / 2f + 6);

        batch.draw(silverKeyIcon, inventoryX, inventoryY - iconSize * 2 - 8, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.SILVER_KEY), inventoryX + iconSize + paddingY, inventoryY - iconSize * 1.5f - 8 + 6);

        batch.draw(goldenKeyIcon, inventoryX, inventoryY - iconSize * 3 - 16, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.GOLDEN_KEY), inventoryX + iconSize + paddingY, inventoryY - iconSize * 2.5f - 16 + 6);

        if (locketIcon != null) {
            batch.draw(locketIcon, inventoryX, inventoryY - iconSize * 4 - 24, iconSize, iconSize);
            font.draw(batch, "x " + inventory.getItemCount(ItemType.LOCKET), inventoryX + iconSize + paddingY, inventoryY - iconSize * 3.5f - 24 + 6);
        }

        batch.draw(wandIcon, inventoryX, inventoryY - iconSize * 5 - 32, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.WAND), inventoryX + iconSize + paddingY, inventoryY - iconSize * 4.5f - 32 + 6);

        batch.end();

        // Mostra ponto vermelho na porta de saída se tiveres o medalhão
        if (inventory.getItemCount(ItemType.LOCKET) > 0) {
            ShapeRenderer shapeRenderer = new ShapeRenderer();
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.circle(15 * TILE_SIZE + TILE_SIZE / 2f, 1 * TILE_SIZE + TILE_SIZE / 2f, 6);
            shapeRenderer.end();
            shapeRenderer.dispose();
        }
    }

    // Verifica se deves sair do ecrã
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

    // Movimento com teclas (alternativo ao clique)
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

    // Impede que a câmara saia dos limites do mapa
    private void clampCameraPosition() {
        float hw = camera.viewportWidth / 2f;
        float hh = camera.viewportHeight / 2f;
        camera.position.x = Math.max(hw, Math.min(camera.position.x, mapWidth - hw));
        camera.position.y = Math.max(hh, Math.min(camera.position.y, mapHeight - hh));
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

