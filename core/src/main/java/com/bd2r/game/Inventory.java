package com.bd2r.game;

import com.bd2r.game.Observer.ItemType;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    // Guarda o número de itens por tipo (ex: moedas, chaves, etc.)
    private final Map<ItemType, Integer> items = new HashMap<>();

    // Adiciona um item ao inventário
    public void addItem(ItemType type) {
        items.put(type, items.getOrDefault(type, 0) + 1);
        System.out.println("📦 Apanhado: " + type + " | Total: " + items.get(type));
    }

    // Devolve o total de itens de um certo tipo
    public int getItemCount(ItemType type) {
        return items.getOrDefault(type, 0);
    }

    // Verifica se tem pelo menos um item desse tipo
    public boolean hasItem(ItemType type) {
        return getItemCount(type) > 0;
    }

    // Remove um item (caso exista)
    public void removeItem(ItemType type) {
        if (hasItem(type)) {
            items.put(type, items.get(type) - 1);
        }
    }

    // Devolve o mapa completo de itens (todos os tipos e quantidades)
    public Map<ItemType, Integer> getAllItems() {
        return items;
    }
}
