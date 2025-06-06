package com.bd2r.game.ecs.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

public class AnimationComponent {
    public Map<String, TextureRegion[]> animations = new HashMap<>();
    public String currentDirection = "down";

    public float frameDuration;
    public float elapsedTime = 0;
    public int currentFrame = 0;

    public AnimationComponent(float frameDuration) {
        this.frameDuration = frameDuration;
    }

    public void addAnimation(String direction, TextureRegion[] frames) {
        animations.put(direction, frames);
    }

    public void setDirection(String direction) {
        if (!direction.equals(currentDirection)) {
            currentDirection = direction;
            currentFrame = 0;
            elapsedTime = 0;
        }
    }

    public void update(float delta) {
        TextureRegion[] frames = animations.get(currentDirection);
        if (frames == null || frames.length == 0) return;

        elapsedTime += delta;
        if (elapsedTime >= frameDuration) {
            elapsedTime = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }

    public TextureRegion getCurrentFrame() {
        TextureRegion[] frames = animations.get(currentDirection);
        return (frames != null && frames.length > 0) ? frames[currentFrame] : null;
    }
}


