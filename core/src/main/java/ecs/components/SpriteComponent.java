package ecs.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpriteComponent {
    public TextureRegion region;

    public SpriteComponent(TextureRegion region) {
        this.region = region;
    }
}

