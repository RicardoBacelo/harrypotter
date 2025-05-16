package com.bd2r.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.EntityManager;
import com.bd2r.game.ecs.components.AnimationComponent;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.VelocityComponent;
import com.bd2r.game.ecs.systems.MovementSystem;
import com.bd2r.game.ecs.systems.RenderSystem;
import com.bd2r.game.factory.EntityFactory;

import java.util.List;

public class GameScreen implements Screen {

    private final EntityManager entityManager = new EntityManager();
    private final MovementSystem movementSystem = new MovementSystem();
    private RenderSystem renderSystem;

    private SpriteBatch batch;
    private Texture playerTexture;

    private Entity player;
    public TextureRegion[] walkUpFrames, walkDownFrames, walkLeftFrames, walkRightFrames;


    @Override
    public void show() {
        batch = new SpriteBatch();
        renderSystem = new RenderSystem(batch);

        walkDownFrames = new TextureRegion[3];
        walkUpFrames = new TextureRegion[3];
        walkRightFrames = new TextureRegion[3];
        walkLeftFrames = new TextureRegion[3];

        playerTexture = new Texture(Gdx.files.internal("player1.png"));

// Cada linha tem 32 de altura, cada coluna 32 de largura
        for (int i = 0; i < 3; i++) {
            walkDownFrames[i]    = new TextureRegion(playerTexture, i * 32, 0,   32, 32);  // linha 1
            walkLeftFrames[i] = new TextureRegion(playerTexture, i * 32, 32,  32, 32);  // linha 2
            walkRightFrames[i]  = new TextureRegion(playerTexture, i * 32, 64,  32, 32);  // linha 3
            walkUpFrames[i]  = new TextureRegion(playerTexture, i * 32, 96,  32, 32);  // linha 4
        }

        /*walkDownFrames[0] = new TextureRegion(playerTexture, 0, 96, 32, 32);
        walkDownFrames[1] = new TextureRegion(playerTexture, 32, 96, 32, 32);
        walkDownFrames[2] = new TextureRegion(playerTexture, 64, 96, 32, 32);
        */


        TextureRegion playerFrame = new TextureRegion(playerTexture, 0, 96, 32, 32);

        AnimationComponent walkAnim = new AnimationComponent(walkUpFrames, 0.2f);

        player = EntityFactory.createPlayer(100, 100, walkUpFrames[1]);
        player.addComponent(walkAnim);
        entityManager.addEntity(player);
    }

    @Override
    public void render(float delta) {
        handleInput();

        movementSystem.update(entityManager.getEntities(), delta);

        player.getComponent(AnimationComponent.class).update(delta);

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderSystem.render(entityManager.getEntities());
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

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
    }
}
