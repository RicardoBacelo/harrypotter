package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import core.MainGame;
import ecs.EntityManager;
import ecs.components.AnimationComponent;
import ecs.systems.MovementSystem;
import ecs.systems.RenderSystem;
import factory.EntityFactory;
import observer.ItemType;
import observer.managers.CoinManager;
import observer.managers.GoldenKeyManager;
import observer.managers.SilverKeyManager;
import ui.Inventory;
import items.Locket;
import observer.managers.LocketManager;
import ecs.Entity;
import ecs.components.PositionComponent;

public class HagridHouseScreen implements Screen{
    private final MainGame game;
    private final EntityManager entityManager;
    private final RenderSystem renderSystem;
    private SpriteBatch batch;

    // Câmera e texturas
    private OrthographicCamera camera;
    private Texture mapTexture;
    private Texture playerTexture;
    private Texture locketTexture;
    private Texture coinIcon, silverKeyIcon, goldenKeyIcon;
    private BitmapFont font;

    // Entidades e sistemas
    private Entity player;
    private LocketManager locketManager;
    private Locket locket;

    private CoinManager coinManager;
    private SilverKeyManager silverKeyManager;
    private GoldenKeyManager goldenKeyManager;

    private final Inventory inventory;

    private TextureRegion[] walkUpFrames, walkDownFrames, walkLeftFrames, walkRightFrames;

    public HagridHouseScreen(MainGame game) {
        this.game = game;
        this.renderSystem = new RenderSystem();
        this.entityManager = new EntityManager();
        this.inventory = game.getInventory();
    }

    @Override public void show() {
        // Initialize all resources when the screen is shown
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Load the texture
        mapTexture = new Texture(Gdx.files.internal("casa.jpg"));
        playerTexture = new Texture(Gdx.files.internal("hero1.png"));
        locketTexture = new Texture(Gdx.files.internal("locket.png"));

        // 3) Carrega texturas de ícones do inventário
        coinIcon       = new Texture(Gdx.files.internal("coin.png"));
        silverKeyIcon  = new Texture(Gdx.files.internal("House_Key.png"));
        goldenKeyIcon  = new Texture(Gdx.files.internal("Castle_Key.png"));

        //Fonts
        font = new BitmapFont();
        font.getData().setScale(1.0f);

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

        player = EntityFactory.createPlayer(485, 60, walkDownFrames[1]);
        player.addComponent(new AnimationComponent(walkUpFrames, 0.2f));

        PositionComponent pComponent = player.getComponent(PositionComponent.class);
        if (pComponent == null) {
            pComponent = new PositionComponent();
            pComponent.x = 485;
            pComponent.y = 60;
            player.addComponent(pComponent);
        }
        entityManager.addEntity(player);

        locketManager = new LocketManager();
        coinManager = new CoinManager();
        silverKeyManager = new SilverKeyManager();
        goldenKeyManager = new GoldenKeyManager();

        locket = new Locket(390, 770);
        locketManager.addLocket(locket, this);

        // 9) Centra a camara na área do mapa
        int mapWidth  = mapTexture.getWidth();
        int mapHeight = mapTexture.getHeight();
        camera.position.set(mapWidth / 2f, mapHeight / 2f, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        PositionComponent pos = player.getComponent(PositionComponent.class);

        if (pos != null) {
            locketManager.updateAndNotifyLockets(pos.x,  pos.y, inventory);
            locketManager.render(batch, locketTexture, delta);
        }

        // 3) Atualiza câmera para “seguir” o player, se for o caso
        if (pos != null) {
            camera.position.set(pos.x + 16, pos.y + 16, 0);
            clampCameraPosition();
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // 4) Tudo em um único batch.begin()…batch.end()
        batch.begin();

        // 4.1) Desenha o mapa e entidades (incluindo o próprio player)
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());

        // 4.2) Atualiza e desenha o locket (usa a posição real do player, via pos.x e pos.y)
        if (pos != null && locketManager != null) {
            locketManager.updateAndNotifyLockets(pos.x, pos.y, inventory);
            locketManager.render(batch, locketTexture, delta);
            // Se o seu LocketManager não tiver .render(), faça manual:
            // float origW = locketTexture.getWidth(), origH = locketTexture.getHeight(), scale = 0.1f;
            // batch.draw(locketTexture,
            //    locket.getX() - (origW*scale)/2,
            //    locket.getY() - (origH*scale)/2,
            //    origW*scale, origH*scale);
        }

        // 4.3) Desenha ícones do inventário no canto da tela/follow camera
        float invX = camera.position.x + camera.viewportWidth/2f - 200;
        float invY = camera.position.y - camera.viewportHeight/2f + 100;
        int   iconSize = 24;
        float padY = 4f;

        // 4.3.1) Moedas
        batch.draw(coinIcon, invX, invY - iconSize, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.COIN),
            invX + iconSize + padY, invY - iconSize/2f + 6);

        // 4.3.2) Chave Prata
        batch.draw(silverKeyIcon, invX, invY - iconSize*2 - 8, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.SILVER_KEY),
            invX + iconSize + padY, invY - iconSize*1.5f - 8 + 6);

        // 4.3.3) Chave Dourada
        batch.draw(goldenKeyIcon, invX, invY - iconSize*3 - 16, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.GOLDEN_KEY),
            invX + iconSize + padY, invY - iconSize*2.5f - 16 + 6);

        batch.end();
    }

    private void clampCameraPosition() {
        float halfWidth  = camera.viewportWidth  / 2f;
        float halfHeight = camera.viewportHeight / 2f;
        float minX = halfWidth;
        float maxX = mapTexture.getWidth()  - halfWidth;
        float minY = halfHeight;
        float maxY = mapTexture.getHeight() - halfHeight;

        float cx = camera.position.x;
        float cy = camera.position.y;
        if (cx < minX) cx = minX;
        if (cx > maxX) cx = maxX;
        if (cy < minY) cy = minY;
        if (cy > maxY) cy = maxY;
        camera.position.set(cx, cy, 0);
    }


    @Override public void resize(int width, int height) {
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }

    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (mapTexture != null) mapTexture.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (locketTexture != null) locketTexture.dispose();
        if (coinIcon != null)      coinIcon.dispose();
        if (silverKeyIcon != null) silverKeyIcon.dispose();
        if (goldenKeyIcon != null) goldenKeyIcon.dispose();
        if (font != null)          font.dispose();

        // Se seus managers tiverem dispose()
        if (locketManager != null)     locketManager.dispose();
        if (coinManager != null)       coinManager.dispose();
        if (silverKeyManager != null)  silverKeyManager.dispose();
        if (goldenKeyManager != null)  goldenKeyManager.dispose();
    }
}


