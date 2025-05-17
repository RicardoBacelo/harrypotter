package com.bd2r.game;

import com.badlogic.gdx.Gdx;

import java.util.*;

public class MapLoader {
    public static int[][] loadMap(String path) {
        List<int[]> lines = new ArrayList<>();

        try (Scanner scanner = new Scanner(Gdx.files.internal(path).reader())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split(" ");
                int[] row = new int[tokens.length];

                for (int i = 0; i < tokens.length; i++) {
                    row[i] = Integer.parseInt(tokens[i]);
                }

                lines.add(row);
            }
        }

        int[][] map = new int[lines.size()][lines.get(0).length];
        for (int y = 0; y < lines.size(); y++) {
            map[y] = lines.get(y);
        }

        return map;
    }
}
