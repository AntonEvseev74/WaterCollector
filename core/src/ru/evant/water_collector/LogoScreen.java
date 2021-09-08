package ru.evant.water_collector;

/*
 * Экран с логотимпом автора
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;

public class LogoScreen implements Screen {

    final Drop game;
    OrthographicCamera camera;

    Texture logoImage;
    Rectangle logo;

    long lastTime;

    public LogoScreen(final Drop game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        //лого
        logoImage = new Texture("evant_blackandwhite_logo.png");
        logo = new Rectangle();
        logo.x = 800 / 2 - 700 / 2;
        logo.y = 480 / 2 - 440 / 2;
        logo.width = 700;
        logo.height = 480;
        lastTime = TimeUtils.millis();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(logoImage, logo.x, logo.y);
        game.batch.end();

        // Задержка перед переходом на экран со стартовым меню
        if (TimeUtils.millis() - lastTime > 5000) game.setScreen(new MainMenuScreen(game));

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
