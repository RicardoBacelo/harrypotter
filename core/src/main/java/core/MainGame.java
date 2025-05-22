package core;

import Screens.MenuScreen;
import com.badlogic.gdx.Game;
import ui.Inventory;

public class MainGame extends Game {

    private final Inventory inventory = new Inventory();

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void create() {
        System.out.println("🟢 MainGame: iniciado");
        setScreen(new MenuScreen(this));
    }
}
