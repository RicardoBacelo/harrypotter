package com.bd2r.game.factory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.bd2r.game.ecs.Entity;
import com.bd2r.game.ecs.components.AnimationComponent;
import com.bd2r.game.ecs.components.PositionComponent;
import com.bd2r.game.ecs.components.SpriteComponent;
import com.bd2r.game.ecs.components.VelocityComponent;

public class EntityFactory {

    public static Entity createPlayer(float x, float y, Texture spriteSheet) {
        Entity player = new Entity();

        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, 32, 32);

        TextureRegion[] walkDownFrames  = tmp[0];
        TextureRegion[] walkLeftFrames  = tmp[1];
        TextureRegion[] walkRightFrames = tmp[2];
        TextureRegion[] walkUpFrames    = tmp[3];

        AnimationComponent anim = new AnimationComponent(0.2f);
        anim.addAnimation("down", walkDownFrames);
        anim.addAnimation("up", walkUpFrames);
        anim.addAnimation("left", walkLeftFrames);
        anim.addAnimation("right", walkRightFrames);
        anim.setDirection("down");

        player.addComponent(new PositionComponent(x, y));
        player.addComponent(new VelocityComponent(0f, 0f, 100f));
        player.addComponent(anim);
        player.addComponent(new SpriteComponent(anim.getCurrentFrame(), 1f));

        return player;
    }

    public static Entity createOwl(float x, float y, Texture owlTexture) {
        Entity owl = new Entity();

        // Dividir spritesheet em 4 colunas × 3 linhas (12 frames)
        TextureRegion[][] tmp = TextureRegion.split(owlTexture, 32, 32);
        TextureRegion[] frames = new TextureRegion[9];
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                frames[index++] = tmp[row][col];
            }
        }

        AnimationComponent anim = new AnimationComponent(0.1f); // velocidade da animação
        anim.addAnimation("fly", frames);
        anim.setDirection("fly");

        owl.addComponent(new PositionComponent(x, y));
        owl.addComponent(new VelocityComponent(0f, 0f, 50f));
        owl.addComponent(anim);
        owl.addComponent(new SpriteComponent(anim.getCurrentFrame(), 1f));

        return owl;
    }
}
