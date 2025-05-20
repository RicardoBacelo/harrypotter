package com.bd2r.game;

import com.bd2r.game.Observer.ItemType;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private final Map<ItemType, Integer> items = new HashMap<>();

    public void addItem(ItemType type) {
        items.put(type, items.getOrDefault(type, 0) + 1);
        System.out.println("📦 Apanhado: " + type + " | Total: " + items.get(type));
    }

    public int getItemCount(ItemType type) {
        return items.getOrDefault(type, 0);
    }

    public boolean hasItem(ItemType type) {
        return getItemCount(type) > 0;
    }

    public void removeItem(ItemType type) {
        if (hasItem(type)) {
            items.put(type, items.get(type) - 1);
        }
    }

    public Map<ItemType, Integer> getAllItems() {
        return items;
    }
}
