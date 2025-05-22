package ecs.components;

import World.pathfinding.Node;

import java.util.ArrayList;
import java.util.List;

public class PathComponent {
    public List<Node> path;

    public PathComponent() {
        this.path = new ArrayList<>();
    }

    public void setPath(List<Node> newPath) {
        this.path = new ArrayList<>(newPath);
    }
}

