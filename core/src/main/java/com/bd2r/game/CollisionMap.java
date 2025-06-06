package com.bd2r.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class CollisionMap {
    private final List<Rectangle> collisionRects = new ArrayList<>();

    public CollisionMap(String tmxFilePath, String objectLayerName) {
        TiledMap map = new TmxMapLoader().load(tmxFilePath);
        MapLayer layer = map.getLayers().get(objectLayerName);

        if (layer == null) {
            Gdx.app.error("CollisionMap", "Layer not found: " + objectLayerName);
            return;
        }

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                RectangleMapObject rectObject = (RectangleMapObject) object;
                collisionRects.add(rectObject.getRectangle());
            }
        }
    }

    public boolean isBlocked(float x, float y, float width, float height) {
        Rectangle playerRect = new Rectangle(x, y, width, height);
        for (Rectangle rect : collisionRects) {
            if (rect.overlaps(playerRect)) {
                return true;
            }
        }
        return false;
    }


    public List<Rectangle> getCollisionRects() {
        return collisionRects;
    }
}
