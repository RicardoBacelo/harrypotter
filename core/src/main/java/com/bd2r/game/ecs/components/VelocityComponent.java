package com.bd2r.game.ecs.components;

public class VelocityComponent {
    public float vx;
    public float vy;
    public float speed;

    public VelocityComponent(float vx, float vy, float speed) {
        this.vx = vx;
        this.vy = vy;
        this.speed = speed;
    }
}
