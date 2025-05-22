package ecs.components;

public class PositionComponent {
    public float x;
    public float y;

    public PositionComponent() {
        this.x = 0;
        this.y = 0;
    }

    public PositionComponent(float x, float y) {
        this.x = x;
        this.y = y;
    }
}

