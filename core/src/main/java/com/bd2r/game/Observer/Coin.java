package com.bd2r.game.Observer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bd2r.game.Inventory;


public class Coin {
    //Posição da moeda no mapa
    private final float x, y;

    //Estado inicial da moeda
    private boolean collected = false;
    private boolean animationFinished = false;

    //Tamanho da moeda
    public static final float COIN_WIDTH = 32f;
    public static final float COIN_HEIGHT = 32f;

    //Colisão no centro da moeda
    private static float radius = COIN_WIDTH / 2f;

    // Animação ao apanhar moeda
    private float rotation = 0f;
    private float scale = 1f;
    private float alpha = 1f;
    private float animationTime = 0f;
    private final float animationDuration = 0.5f; // 0.5 segundos

    // Construtor que define a posição da moeda
    public Coin(float x, float y) {
        this.x = x;
        this.y = y;
    }

    //Atualização do estado da moeda
    public void update(float playerX, float playerY, Inventory inventory) {
        if (!collected && isNear(playerX, playerY)) {
            collected = true;
            animationTime = 0f;
            inventory.addItem(ItemType.COIN);
            System.out.println("Moeda apanhada.");
        }
    }

    // Verifica se o jogador está suficientemente perto da moeda
    private boolean isNear(float playerX, float playerY) {
        float dx = playerX - (x + COIN_WIDTH / 2);
        float dy = playerY - (y + COIN_HEIGHT / 2);
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
        float originX = COIN_WIDTH / 2f;
        float originY = COIN_HEIGHT / 2f;

        // Desenhar com rotação, escala e origem ajustada
        batch.draw(texture, x, y, originX, originY,
            COIN_WIDTH, COIN_HEIGHT, scale, scale, rotation, 0, 0,
            texture.getWidth(), texture.getHeight(), false, false);

        // Restaurar cor original do batch
        batch.setColor(originalColor);
    }

    //Verifica se a moeda foi apanhada
    public boolean isCollected() {
        return collected;
    }

    //Verifica se a animação da moeda terminou
    public boolean isAnimationFinished() {
        return animationFinished;
    }
}
