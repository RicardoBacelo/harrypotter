package com.bd2r.game.ecs.components;

public class VelocityComponent {
    public float vx;
    public float vy;

    public VelocityComponent() {
        this.vx = 0;
        this.vy = 0;
    }

    public VelocityComponent(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }
}
