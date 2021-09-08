package ru.evant.water_collector;

/*
 * Логика игры:
 *
 * Нужно ведром ловить капли воды.
 *
 * Очки:
 * +1 -> Капля поймана
 * -2 -> Капля не поймана
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public class GameScreen implements Screen {

    final Drop game;

    OrthographicCamera camera;
    SpriteBatch batch;

    Texture dropImage;
    Texture bucketImage;
    Sound dropSound;
    Music rainMusic;

    Rectangle bucket;

    Vector3 touchPos;
    Array<Rectangle> raindrops;

    long lastDropTime;
    int dropsGathered;
    String score = "Score: ";

    public GameScreen(final Drop game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Const.WIDTH_SCREEN, Const.HEIGHT_SCREEN);

        batch = new SpriteBatch();
        touchPos = new Vector3();

        dropImage = new Texture("droplet.png");
        bucketImage = new Texture("bucket.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));

        //Включаем музыку
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        //ведро
        bucket = new Rectangle();
        bucket.x = Const.WIDTH_SCREEN / 2 - 64 / 2;
        bucket.y = 20;
        bucket.width = Const.SIZE_IMAGE - 24; // (Размер для вычисления столкновения)
        bucket.height = Const.SIZE_IMAGE - 24; // (Размер для вычисления столкновения)

        //капля
        raindrops = new Array<>();
        spawnRaindrop();
    }

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, Const.WIDTH_SCREEN - Const.SIZE_IMAGE);
		raindrop.y = Const.HEIGHT_SCREEN;
		raindrop.width = Const.SIZE_IMAGE;
		raindrop.height = Const.SIZE_IMAGE;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.draw(game.batch, score + dropsGathered, 20, 470);
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        game.batch.end();

        // <=> Слушатель нажатия на экран
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = (int) (touchPos.x - Const.SIZE_IMAGE / 2);
        }

        // <=> Слушатели нажатия кнопок клавиатуры: влево и вправо
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            bucket.x -= 200 * Gdx.graphics.getDeltaTime(); // Скорость дыижения ведра влево
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            bucket.x += 200 * Gdx.graphics.getDeltaTime(); // Скорость дыижения ведра вправо

        // <=> Ограничитель движения ведра по оси X
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > Const.WIDTH_SCREEN - Const.SIZE_IMAGE) bucket.x = Const.WIDTH_SCREEN - Const.SIZE_IMAGE;

        // <=> Время для создания новой капли
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

        // Запускам капли в цикле
        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime(); // Скорость падения капли

            // <=> Удаление капли, если она ушла за пределы экрана по оси Y
            if (raindrop.y + Const.SIZE_IMAGE < 0) {
                iter.remove();
                dropsGathered -= 2; // -2, капля не поймана
            }

            // <=>  столкновение капли с ведром
            if (raindrop.overlaps(bucket)) {
                dropsGathered++; // +1, капля поймана
                dropSound.play();
                iter.remove();
            }
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
        batch.dispose();
        bucketImage.dispose();
        dropImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }

    @Override
    public void show() {
        rainMusic.play();
    }
}
