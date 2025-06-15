package com.bd2r.game.ecs.components;

import com.bd2r.game.pathfinder.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PathComponent {
    public Queue<Node> path = new LinkedList<>();

    public void setPath(List<Node> nodes) {
        path.clear();
        path.addAll(nodes);
    }
}

