package com.bd2r.game.Observer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SilverKey implements Observer{
    private final float x, y;

    //Estado inicial da chave
    private boolean collected = false;
    private boolean animationFinished = false;

    //Definição do tamanho da chave
    public static final float SILVERKEY_WIDTH = 32f;
    public static final float SILVERKEY_HEIGHT = 32f;

    //Colisão no centro da chave
    private static float radius = SILVERKEY_WIDTH / 2f;

    // Animação
    private float rotation = 0f;
    private float scale = 1f;
    private float alpha = 1f;
    private float animationTime = 0f;
    private final float animationDuration = 0.5f; // 0.5 segundos

    public SilverKey(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void update(float playerX, float playerY) {
        if (!collected && isNear(playerX, playerY)) {
            collected = true;
            animationTime = 0f;
            System.out.println("Chave apanhada.");
        }
    }

    private boolean isNear(float playerX, float playerY) {
        float dx = playerX - (x + SILVERKEY_WIDTH / 2);
        float dy = playerY - (y + SILVERKEY_HEIGHT / 2);
        return Math.sqrt(dx * dx + dy * dy) <= radius;
    }

    //Desenho da chave
    public void render(SpriteBatch batch, Texture texture, float delta) {
        if (animationFinished) return;

        //Caso apanhada efetua a animação da chave
        if (collected) {
            //System.out.println("🎬 A iniciar a animação da chave");
            animationTime += delta;

            // Atualizar propriedades da animação
            float progress = animationTime / animationDuration;
            if (progress >= 1f) {
                progress = 1f;
                animationFinished = true;
                System.out.println("🟢 Animação da chave finalizada.");
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
        float originX = SILVERKEY_WIDTH / 2f;
        float originY = SILVERKEY_HEIGHT / 2f;

        // Desenhar com rotação, escala e origem ajustada
        batch.draw(texture, x, y, originX, originY,
            SILVERKEY_WIDTH, SILVERKEY_HEIGHT, scale, scale, rotation, 0, 0,
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
}
