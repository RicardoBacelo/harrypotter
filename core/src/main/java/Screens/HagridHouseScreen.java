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
import ecs.components.PositionComponent;
import ecs.systems.RenderSystem;
import factory.EntityFactory;
import jdk.internal.org.jline.terminal.Size;
import observer.ItemType;
import observer.managers.LocketManager;
import org.w3c.dom.Text;
import ui.Inventory;
import items.Locket;  // Certifique-se de usar este import exato
import ecs.Entity;

public class HagridHouseScreen implements Screen {

    // Batch e Câmera
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private static final int TILE_SIZE = 32;

    private final MainGame game;
    private final EntityManager entityManager;
    private final RenderSystem renderSystem;


    // Texturas do mundo e do player
    private Texture mapTexture;
    private Texture playerTexture;
    private Texture locketTexture;

    // Ícones do inventário
    private Texture coinIcon;
    private Texture silverKeyIcon;
    private Texture goldenKeyIcon;
    private Texture locketIcon;
    private BitmapFont font;

    // Entidades principais
    private Entity player;
    private LocketManager locketManager;
    private Locket locket;

    // Inventário e a própria instância
    private final Inventory inventory;

    // Animações do player
    private TextureRegion[] walkUpFrames, walkDownFrames, walkLeftFrames, walkRightFrames;


    public HagridHouseScreen(MainGame game) {
        this.game = game;
        this.renderSystem = new RenderSystem();
        this.entityManager = new EntityManager();
        this.inventory = game.getInventory();
    }

    @Override
    public void show() {
        // 1) Inicializa batch e câmera
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 2) Carrega texturas do mapa e do player
        mapTexture    = new Texture(Gdx.files.internal("casa.jpg"));
        playerTexture = new Texture(Gdx.files.internal("hero1.png"));
        locketTexture = new Texture(Gdx.files.internal("locket.png"));

        // 3) Carrega ícones do inventário
        coinIcon       = new Texture(Gdx.files.internal("coin.png"));
        silverKeyIcon  = new Texture(Gdx.files.internal("House_Key.png"));
        goldenKeyIcon  = new Texture(Gdx.files.internal("Castle_Key.png"));
        locketIcon     = new Texture(Gdx.files.internal("locket.png"));

        // 4) Carrega fonte para contadores
        font = new BitmapFont();
        font.getData().setScale(1f);

        // 5) Prepara as animações do player
        walkUpFrames    = new TextureRegion[3];
        walkDownFrames  = new TextureRegion[3];
        walkLeftFrames  = new TextureRegion[3];
        walkRightFrames = new TextureRegion[3];
        for (int i = 0; i < 3; i++) {
            walkDownFrames[i] = new TextureRegion(playerTexture, i * 32, 0, 32, 32);
            walkLeftFrames[i] = new TextureRegion(playerTexture, i * 32, 32, 32, 32);
            walkRightFrames[i] = new TextureRegion(playerTexture, i * 32, 64, 32, 32);
            walkUpFrames[i]   = new TextureRegion(playerTexture, i * 32, 96, 32, 32);
        }

        // 6) Cria o player com posição inicial e animação
        //    Use EntityFactory ou crie manualmente se preferir.
        player = EntityFactory.createPlayer(485, 60, walkDownFrames[1]);
        // Se for usar AnimationComponent:
        player.addComponent(new AnimationComponent(walkUpFrames, 0.2f));

        // Garante que exista um PositionComponent:
        PositionComponent pComp = player.getComponent(PositionComponent.class);
        if (pComp == null) {
            pComp = new PositionComponent();
            pComp.x = 485;
            pComp.y = 60;
            player.addComponent(pComp);
        }
        entityManager.addEntity(player);

        // 7) Inicializa o LocketManager (e depois insere um Locket fixo em (390,770))
        locketManager = new LocketManager();
        locket = new Locket(390, 760);
        locketManager.addLocket(locket, this);

        // 9) Centra a câmara no meio do mapa (opcional)
        int mapWidth  = mapTexture.getWidth();
        int mapHeight = mapTexture.getHeight();
        camera.position.set(mapWidth / 2f, mapHeight / 2f, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        // 1) Limpa a tela
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2) Atualiza animações e lógica de movimento do player (se for necessário)
        PositionComponent pos = player.getComponent(PositionComponent.class);

        // 3) Faz a câmera “seguir” o player (opcional):
        if (pos != null) {
            camera.position.set(pos.x + 16, pos.y + 16, 0);
            clampCameraPosition();
        }
        camera.update();
        batch.setProjectionMatrix(camera.combined);

                // 4) Desenha tudo num único batch
        batch.begin();

        // 4.1) Desenha o mapa e todas as entidades (incluindo o player)
        batch.draw(mapTexture, 0, 0);
        renderSystem.render(batch, entityManager.getEntities());

        // 4.2) Atualiza e desenha o locket (usa a posição atual do player para notificar)
        if (pos != null && locketManager != null) {
            locketManager.updateAndNotifyLockets(pos.x, pos.y, inventory);
            float drawSize = 64f;
            float halfSize = drawSize / 2f;
            float lx = locket.getX();
            float ly = locket.getY();

            batch.draw(
                locketTexture,
                lx - halfSize,
                ly - halfSize,
                drawSize,
                drawSize
            );
        }

        // Desenha o inventário por cima, fixo em canto relativo à câmera
        float invX = camera.position.x + camera.viewportWidth/2f - 100;
        float invY = camera.position.y - camera.viewportHeight/2f + 200;
        int   iconSize = 24;
        float padY     = 4f;

        // Moeda
        batch.draw(coinIcon, invX, invY - iconSize, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.COIN),
            invX + iconSize + padY,
            invY - iconSize/2f + 6);

        // Chave prata
        batch.draw(silverKeyIcon, invX, invY - iconSize*2 - 8, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.SILVER_KEY),
            invX + iconSize + padY,
            invY - iconSize*1.5f - 8 + 6);

        // Chave dourada
        batch.draw(goldenKeyIcon, invX, invY - iconSize*3 - 16, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.GOLDEN_KEY),
            invX + iconSize + padY,
            invY - iconSize*2.5f - 16 + 6);

        // Locket
        batch.draw(locketIcon, invX, invY - iconSize*4 - 24, iconSize, iconSize);
        font.draw(batch, "x " + inventory.getItemCount(ItemType.LOCKET),
            invX + iconSize + padY,
            invY - iconSize*2.5f - 48 + 6);

        batch.end();
    }

    private void clampCameraPosition() {
        float halfW = camera.viewportWidth  / 2f;
        float halfH = camera.viewportHeight / 2f;
        float minX = halfW;
        float maxX = mapTexture.getWidth()  - halfW;
        float minY = halfH;
        float maxY = mapTexture.getHeight() - halfH;

        float cx = camera.position.x;
        float cy = camera.position.y;
        if (cx < minX) cx = minX;
        if (cx > maxX) cx = maxX;
        if (cy < minY) cy = minY;
        if (cy > maxY) cy = maxY;
        camera.position.set(cx, cy, 0);
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) {
            camera.viewportWidth  = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (batch != null)        batch.dispose();
        if (mapTexture != null)   mapTexture.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (locketTexture != null) locketTexture.dispose();
        if (coinIcon != null)      coinIcon.dispose();
        if (silverKeyIcon != null) silverKeyIcon.dispose();
        if (goldenKeyIcon != null) goldenKeyIcon.dispose();
        if (locketIcon != null)    locketIcon.dispose();
        if (font != null)          font.dispose();
        if (locketManager != null)    locketManager.dispose();
    }
}
