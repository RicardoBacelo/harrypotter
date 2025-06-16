package com.bd2r.game;

import com.badlogic.gdx.Gdx;

import java.util.*;

public class MapLoader {

    // Lê um ficheiro .txt com 0s e 1s e converte-o para uma matriz (mapa)
    public static int[][] loadMap(String path) {
        List<int[]> lines = new ArrayList<>();

        // Lê o ficheiro linha por linha
        try (Scanner scanner = new Scanner(Gdx.files.internal(path).reader())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue; // Ignora linhas vazias

                // Divide a linha em tokens (valores separados por espaço)
                String[] tokens = line.split(" ");
                int[] row = new int[tokens.length];

                // Converte cada token para inteiro
                for (int i = 0; i < tokens.length; i++) {
                    row[i] = Integer.parseInt(tokens[i]);
                }

                lines.add(row); // Adiciona a linha convertida à lista
            }
        }

        // Converte a lista para uma matriz 2D
        int[][] map = new int[lines.size()][lines.get(0).length];
        for (int y = 0; y < lines.size(); y++) {
            map[y] = lines.get(y);
        }

        return map; // Devolve o mapa final
    }
}
