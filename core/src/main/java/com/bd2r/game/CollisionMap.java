package com.bd2r.game;

public class CollisionMap {
    private final int[][] map;
    private static final int TILE_SIZE = 32;

    public CollisionMap(String path) {
        this.map = MapLoader.loadMap(path);
    }

    public boolean isBlocked(float x, float y, float width, float height) {
        int tileStartX = (int) (x / TILE_SIZE);
        int tileEndX = (int) ((x + width - 1) / TILE_SIZE);
        int tileStartY = (int) (y / TILE_SIZE);
        int tileEndY = (int) ((y + height - 1) / TILE_SIZE);

        for (int ty = tileStartY; ty <= tileEndY; ty++) {
            for (int tx = tileStartX; tx <= tileEndX; tx++) {
                if (isOutOfBounds(tx, ty) || map[ty][tx] == 0) {
                    return true; // bloqueado ou fora do mapa
                }
            }
        }

        return false; // livre
    }

    private boolean isOutOfBounds(int x, int y) {
        return y < 0 || y >= map.length || x < 0 || x >= map[0].length;
    }
}
