package ru.evant.water_collector;

 /*
 * Главный класс игры
 */

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Drop extends Game {

    SpriteBatch batch;  // используется для отображения текстур и картинок на экране
    BitmapFont font; // используется для отображения текста на экране

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        this.setScreen(new LogoScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    // Освобождаем ресурсы
    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        font.dispose();
    }
}
