package ru.evant.water_collector;

/*
 * Логика игры
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
    int dropsGatchered;
    String score = "Score: ";

    public GameScreen(final Drop game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

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
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 20;
        bucket.width = 64;
        bucket.height = 64;

        //капля
        raindrops = new Array<>();
        spawnRaindrop();
    }

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800 - 64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.draw(game.batch, score + dropsGatchered, 0, 480);
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        game.batch.end();

        // <=> Слушатель нажатия на экран
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = (int) (touchPos.x - 64 / 2);
        }

        // <=> Слушатели нажатия кнопок клавиатуры: влево и вправо
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            bucket.x -= 200 * Gdx.graphics.getDeltaTime(); // Скорость дыижения ведра влево
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            bucket.x += 200 * Gdx.graphics.getDeltaTime(); // Скорость дыижения ведра вправо

        // <=> Ограничитель движения ведра по оси X
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > 800 - 64) bucket.x = 800 - 64;

        // <=> Время для создания новой капли
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

        // Запускам капли в цикле
        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime(); // Скорость падения капли

            // <=> Удаление капли, если она ушла за пределы экрана по оси Y
            if (raindrop.y + 64 < 0) iter.remove();

            // <=>  столкновение капли с ведром
            if (raindrop.overlaps(bucket)) {
                dropsGatchered++;
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
