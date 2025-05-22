package ui;

import observer.ItemType;
import java.util.HashMap;
import java.util.Map;


//Classe que armazena os itens do inventario
public class Inventory {
    //Relaciona items ao inventario conforme tipo
    private final Map<ItemType, Integer> items = new HashMap<>();

    //Adiciona item ao inventario conforme tipo
    public void addItem(ItemType type) {
        items.put(type, items.getOrDefault(type, 0) + 1);
        System.out.println("📦 Apanhado: " + type + " | Total: " + items.get(type));
    }

    //Mostra a quantidade de determinado item do inventario
    public int getItemCount(ItemType type) {
        return items.getOrDefault(type, 0);
    }

    //Verifica se o inventario possui determinado item
    public boolean hasItem(ItemType type) {
        return getItemCount(type) > 0;
    }

    //Remove item do inventario conforme tipo
    public void removeItem(ItemType type) {
        if (hasItem(type)) {
            items.put(type, items.get(type) - 1);
        }
    }

    //Mostra todos os itens existentes no inventario
    public Map<ItemType, Integer> getAllItems() {
        return items;
    }
}
