package com.bd2r.game.Observer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bd2r.game.Inventory;

public class Locket {
    private final float x, y;



    //Estado inicial do locket
    private boolean collected = false;
    private boolean animationFinished = false;

    //Definição do tamanho do locket
    public static final float LOCKET_WIDTH = 64f;
    public static final float LOCKET_HEIGHT = 64f;

    //Colisão no centro da moeda
    private static float radius = LOCKET_WIDTH / 2f;

    // Animação
    private float rotation = 0f;
    private float scale = 1f;
    private float alpha = 1f;
    private float animationTime = 0f;
    private final float animationDuration = 0.5f; // 0.5 segundos

    private static final float PLAYER_WIDTH  = 32f;
    private static final float PLAYER_HEIGHT = 32f;

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
            System.out.println("Locket apanhado.");
        }
    }

    private boolean isNear(float playerX, float playerY) {
        float px = playerX + PLAYER_WIDTH  / 2f;
        float py = playerY + PLAYER_HEIGHT / 2f;
        float wx = x        + LOCKET_WIDTH   / 2f;
        float wy = y        + LOCKET_HEIGHT  / 2f;

        float dx = px - wx;
        float dy = py - wy;
        float distance = (float)Math.sqrt(dx*dx + dy*dy);

        // metade da largura do jogador + metade da largura da varinha
        float collisionRadius = (PLAYER_WIDTH + LOCKET_WIDTH) / 2f;
        return distance <= collisionRadius;
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
                System.out.println("🟢 Animação do locket finalizada.");
            }
            //Efeitos da animação
            rotation = 360f * progress;
            scale = Math.max(0.01f, 1f - progress);
            alpha = Math.max(0f, 1f - progress);
        }

        // Aplicar cor com alpha
        Color originalColor = batch.getColor().cpy();
        batch.setColor(1f, 1f, 1f, alpha);

        // Centro do Locket
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
