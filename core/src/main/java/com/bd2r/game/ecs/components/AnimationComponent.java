package com.bd2r.game.ecs.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationComponent {
    public TextureRegion[] frames;
    public float frameDuration;
    public float elapsedTime = 0;
    public int currentFrame = 0;

    public AnimationComponent(TextureRegion[] frames, float frameDuration) {
        this.frames = frames;
        this.frameDuration = frameDuration;
    }

    public TextureRegion getCurrentFrame() {
        return frames[currentFrame];
    }

    public void update(float delta) {
        elapsedTime += delta;
        if (elapsedTime >= frameDuration) {
            elapsedTime = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }
}

