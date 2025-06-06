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

        // Dividir o spriteSheet em frames de 32x32
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet, 32, 32);

        TextureRegion[] walkDownFrames  = tmp[0]; // linha 0
        TextureRegion[] walkLeftFrames  = tmp[1]; // linha 1
        TextureRegion[] walkRightFrames = tmp[2]; // linha 2
        TextureRegion[] walkUpFrames    = tmp[3]; // linha 3

        // Criar componente de animação com todas as direções
        AnimationComponent anim = new AnimationComponent(0.2f);
        anim.addAnimation("down", walkDownFrames);
        anim.addAnimation("up", walkUpFrames);
        anim.addAnimation("left", walkLeftFrames);
        anim.addAnimation("right", walkRightFrames);
        anim.setDirection("down"); // direção inicial

        // Adicionar componentes
        player.addComponent(new PositionComponent(x, y));
        player.addComponent(new VelocityComponent(0f, 0f, 100f));
        player.addComponent(anim);
        player.addComponent(new SpriteComponent(walkDownFrames[0], 1f)); // frame inicial + escala

        return player;
    }
}

