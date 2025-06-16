package com.bd2r.game;

public class CollisionMap {
    private final int[][] map;
    private static final int TILE_SIZE = 32;

    public CollisionMap(String path) {
        // Carrega o mapa de colisões a partir de ficheiro (1 = livre, 0 = bloqueado)
        this.map = MapLoader.loadMap(path);
    }

    // Verifica se uma área (x, y, largura, altura) está bloqueada
    public boolean isBlocked(float x, float y, float width, float height) {
        // Converte coordenadas em tiles
        int tileStartX = (int) (x / TILE_SIZE);
        int tileEndX = (int) ((x + width - 1) / TILE_SIZE);
        int tileStartY = (int) (y / TILE_SIZE);
        int tileEndY = (int) ((y + height - 1) / TILE_SIZE);

        // Verifica todos os tiles ocupados pela área
        for (int ty = tileStartY; ty <= tileEndY; ty++) {
            for (int tx = tileStartX; tx <= tileEndX; tx++) {
                // Se estiver fora dos limites ou for bloqueado (0), devolve true
                if (isOutOfBounds(tx, ty) || map[ty][tx] == 0) {
                    return true;
                }
            }
        }

        // Se não encontrou bloqueios, está livre
        return false;
    }

    // Verifica se o tile está fora dos limites do mapa
    private boolean isOutOfBounds(int x, int y) {
        return y < 0 || y >= map.length || x < 0 || x >= map[0].length;
    }
}
