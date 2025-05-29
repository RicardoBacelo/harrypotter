package com.bd2r.game.Observer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import ui.Inventory;
import observer.ItemType;

public class Locket {
    private final float x, y;

    //Estado inicial do locket
    private boolean collected = false;
    private boolean animationFinished = false;

    //Definição do tamanho do locket
    public static final float LOCKET_WIDTH = 32f;
    public static final float LOCKET_HEIGHT = 32f;

    //Colisão no centro da moeda
    private static float radius = LOCKET_WIDTH / 2f;

    // Animação
    private float rotation = 0f;
    private float scale = 1f;
    private float alpha = 1f;
    private float animationTime = 0f;
    private final float animationDuration = 0.5f; // 0.5 segundos

    public Locket(float x, float y) {
        this.x = x;
        this.y = y;
    }

    //@Override
    public void update(float playerX, float playerY, Inventory inventory) {
        if (!collected && isNear(playerX, playerY)) {
            collected = true;
            animationTime = 0f;
            inventory.addItem(ItemType.LOCKET);
            System.out.println("Moeda apanhada.");
        }
    }

    private boolean isNear(float playerX, float playerY) {
        float dx = playerX - (x + LOCKET_WIDTH / 2);
        float dy = playerY - (y + LOCKET_HEIGHT / 2);
        return Math.sqrt(dx * dx + dy * dy) <= radius;
    }

    //Desenho da moeda
    public void render(SpriteBatch batch, Texture texture, float delta) {
        if (animationFinished) return;

        //Caso apanhada efetua a animação da moeda
        if (collected) {
            //System.out.println("🎬 Iniciando animação da moeda");
            animationTime += delta;

            // Atualizar propriedades da animação
            float progress = animationTime / animationDuration;
            if (progress >= 1f) {
                progress = 1f;
                animationFinished = true;
                System.out.println("🟢 Animação da moeda finalizada.");
            }
            //Efeitos da animação
            rotation = 360f * progress;
            scale = Math.max(0.01f, 1f - progress);
            alpha = Math.max(0f, 1f - progress);
        }

        // Aplicar cor com alpha
        Color originalColor = batch.getColor().cpy();
        batch.setColor(1f, 1f, 1f, alpha);

        // Centro da moeda
        float originX = LOCKET_WIDTH / 2f;
        float originY = LOCKET_HEIGHT / 2f;

        // Desenhar com rotação, escala e origem ajustada
        batch.draw(texture, x, y, originX, originY,
            LOCKET_WIDTH, LOCKET_HEIGHT, scale, scale, rotation, 0, 0,
            texture.getWidth(), texture.getHeight(), false, false);

        // Restaurar cor original do batch
        batch.setColor(originalColor);
    }

    public boolean isCollected() {
        return collected;
    }

    public boolean isAnimationFinished() {
        return animationFinished;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
