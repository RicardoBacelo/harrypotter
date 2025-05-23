package com.bd2r.game.ecs.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpriteComponent {
    public TextureRegion region;
    public float scale = 1f;

    public SpriteComponent(TextureRegion region) {
        this.region = region;
    }
}

