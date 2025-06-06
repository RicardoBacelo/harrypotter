package com.bd2r.game.ecs.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpriteComponent {
    public TextureRegion region; // Região do sprite na textura
    public float scale = 1.0f;   // Escala do sprite (tamanho)

    public SpriteComponent(TextureRegion region, float scale) {
        this.region = region;
        this.scale = scale;
    }
}
