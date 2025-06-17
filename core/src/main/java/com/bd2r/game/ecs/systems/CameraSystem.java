package com.bd2r.game.ecs.systems;

import com.badlogic.gdx.graphics.OrthographicCamera;

public class CameraSystem {



    public static void clampCameraPosition(int mapWidth, int mapHeight, OrthographicCamera camera) {

        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        float minX = halfWidth;
        float maxX = mapWidth - minX;
        float minY = halfHeight;
        float maxY = mapHeight - minY;

        camera.position.x = Math.max(minX, Math.min(camera.position.x, maxX));
        camera.position.y = Math.max(minY, Math.min(camera.position.y, maxY));
    }
}
