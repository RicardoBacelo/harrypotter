package com.bd2r.game.pathfinder;

import java.util.Objects;

public class Node {
    public int x, y;            // Posição no mapa (tile)
    public float gCost, hCost;  // g = custo desde o início, h = custo estimado até ao fim
    public Node parent;         // Nó anterior no caminho (para reconstrução)

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // f = g + h, usado para ordenar nós no A*
    public float getFCost() {
        return gCost + hCost;
    }

    // Dois nós são iguais se tiverem as mesmas coordenadas
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) return false;
        Node other = (Node) obj;
        return this.x == other.x && this.y == other.y;
    }

    // Necessário para usar Node em HashSet/HashMap (como closedSet)
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

