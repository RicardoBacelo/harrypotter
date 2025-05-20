package com.bd2r.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bd2r.game.Observer.*;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.components.*;
import com.bd2r.game.ecs.systems.MovementSystem;
import com.bd2r.game.ecs.systems.RenderSystem;
import com.bd2r.game.factory.EntityFactory;
import com.bd2r.game.pathfinder.AStarPathfinder;
import com.bd2r.game.pathfinder.Node;
import com.bd2r.game.Observer.ItemType;
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

    private final MainGame game;
    private final Inventory inventory;
    private BitmapFont font;
    private Texture whitePixel;

    public GameScreen(MainGame game) {
        this.game = game;
        this.inventory = game.getInventory();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        //inventory = new Inventory();
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
            walkDownFrames[i]  = new TextureRegion(playerTexture, i * 32, 0, 32, 32);
            walkLeftFrames[i]  = new TextureRegion(playerTexture, i * 32, 32, 32, 32);
            walkRightFrames[i] = new TextureRegion(playerTexture, i * 32, 64, 32, 32);
            walkUpFrames[i]    = new TextureRegion(playerTexture, i * 32, 96, 32, 32);
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
                int tileX = (int) ((Gdx.input.getX() + camera.position.x - camera.viewportWidth / 2) / TILE_SIZE);
                int tileY = (int) ((Gdx.graphics.getHeight() - Gdx.input.getY() + camera.position.y - camera.viewportHeight / 2) / TILE_SIZE);

                int startX = (int) (pos.x / TILE_SIZE);
                int startY = (int) (pos.y / TILE_SIZE);

                System.out.println("🖱️ Clicked tile: " + tileX + "," + tileY);
                System.out.println("👣 Player at tile: " + startX + "," + startY);

                AStarPathfinder pathfinder = new AStarPathfinder(MapLoader.loadMap("mapa.txt")); // path corrected
                List<Node> path = pathfinder.findPath(startX, startY, tileX, tileY);

                if (path != null && !path.isEmpty()) {
                    System.out.println("✅ Path found! " + path.size() + " steps.");
                    player.addComponent(new PathComponent());
                    player.getComponent(PathComponent.class).path = path;
                } else {
                    System.out.println("⚠️ No path found.");
                }

            } catch (Exception e) {
                System.err.println("❌ Error on mouse click:");
                e.printStackTrace();
            }
        }



        // Clear screen and draw
        //Gdx.gl.glClearColor(0.1f, 0.1f, 0.3f, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        //batch.setColor(1, 1, 1, 1);
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());
        coinManager.updateAndNotifyCoins(pos.x, pos.y, inventory);
        coinManager.render(batch, coinTexture, delta);
        silverKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
        silverKeyManager.render(batch, silverKeyTexture, delta);
        goldenKeyManager.updateAndNotifyKeys(pos.x, pos.y, inventory);
        goldenKeyManager.render(batch, goldenKeyTexture, delta);


        //UI de inventário
        int padding = 16;
        int slotSize = 24;
        int startX = Gdx.graphics.getWidth() - 200 - padding;
        int startY = padding + slotSize *3;

        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(whitePixel, startX - padding, padding, 180, slotSize * 3 + padding);
        batch.setColor(Color.WHITE);

        font.draw(batch, "Inventário", startX, startY);
        font.draw(batch, inventory.getItemCount(ItemType.COIN) + " x Coins", startX, startY - slotSize);
        font.draw(batch, inventory.getItemCount(ItemType.SILVER_KEY) + " x Silver Keys", startX, startY - slotSize * 2);
        font.draw(batch, inventory.getItemCount(ItemType.GOLDEN_KEY) + " x Golden Keys", startX, startY - slotSize * 3);

        batch.end();
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


