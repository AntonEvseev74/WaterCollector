package ru.evant.water_collector;

/*
 * Стартовое меню игры
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class GameOverScreen implements Screen {

    final Drop game;
    OrthographicCamera camera;

    String forGameOver = "Game Over!"; // Поймайте все капли!
    String forContinue = "Click to try again!"; // Нажмите чтобы продолжить
    String forDescription = "Either the hands are crooked.\nWhether the bucket is full of holes.";
    String forScore = "Your SCORE = ";
    int score;

    public GameOverScreen(final Drop game, int score) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Const.WIDTH_SCREEN, Const.HEIGHT_SCREEN);
        forScore += score;
        this.score = score;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.draw(game.batch, forGameOver, 350, 400);
        game.font.draw(game.batch, forScore, 320, 350);
        if (score < 0) game.font.draw(game.batch, forDescription, 350, 170);
        game.font.draw(game.batch, forContinue, 350, 100);
        game.batch.end();

        //  <=> было ли прикосновение к экрану
        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
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
