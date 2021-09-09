package ru.evant.water_collector;

/*
 * Логика игры:
 *
 * Нужно ведром ловить капли воды.
 * Если капли пойманы очки увеличиваются,
 * в ином случае очки уменьшаются и гремит гром.
 *
 * Очки:
 * +1 -> Капля поймана
 * -2 -> Капля не поймана
 */

/* - разобраться с молниями:
 *      - исправить размеры всех 3х картинок, сделать одинаковыми (200х480)
 *      + сделать, чтобы молнии били разные
 *      + сделать, чтобы удар молнии приходился в центр непойманной капли
 *
 * - сделать сколько не поймано
 * - через 10 пойманых капель увеличивать скорость капель
 * - сделать экран рекордов
 * - сделать меню (кнопк: выход, рекорды)
 * - добавить жизни
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

    Texture backgroundImage;

    Sound dropSound;
    Sound loudlySound;
    Music rainMusic;

    Texture bucketImage;
    Rectangle bucket;

    Vector3 touchPos;

    // Капли
    Texture dropImage;
    Array<Rectangle> raindrops;
    long lastDropTime;

    int dropsGathered;
    String score = "Score: ";
    int dropsSpeed = 200;
    int bucketSpeed = 200;
    int indexSpeed = 2;

    // Молния
    Texture lightningBoltImage;
    Rectangle lightningBolt;
    String[] lightningBolts = {"lightning_bolts_1.png", "lightning_bolts_2.png", "lightning_bolts_3.png"};
    int rndLightningBoltsPath = MathUtils.random(0, 2);
    long lastTimeLightningBolt;

    // Жизни
    Texture liveImage;
    Array<Rectangle> lives;
    int liveCount = 5;

    public GameScreen(final Drop game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Const.WIDTH_SCREEN, Const.HEIGHT_SCREEN);

        batch = new SpriteBatch();
        touchPos = new Vector3();

        backgroundImage = new Texture("background.jpg");
        dropImage = new Texture("droplet.png");
        bucketImage = new Texture("bucket.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
        loudlySound = Gdx.audio.newSound(Gdx.files.internal("loudly.mp3"));

        // Жизни
        liveImage = new Texture("bucket.png");
        lives = new Array<>();
        for (int i = liveCount; i > 0; i--) {
            lives.add(new Rectangle(
                    Const.INDENT * 3 + i * Const.INDENT ,
                    Const.HEIGHT_SCREEN - liveImage.getHeight()/4 - 10,
                    Const.SIZE_IMAGE/4,
                    Const.SIZE_IMAGE/4));
        }

        lightningBoltImage = new Texture(lightningBolts[rndLightningBoltsPath]);

        //Молния
        lightningBolt = new Rectangle();
        lightningBolt.x = Const.WIDTH_SCREEN / 2 - 300 / 2;
        lightningBolt.y = Const.HEIGHT_SCREEN;

        //Включаем музыку
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        //ведро
        bucket = new Rectangle();
        bucket.x = Const.WIDTH_SCREEN / 2 - Const.SIZE_IMAGE / 2;
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
        game.batch.draw(backgroundImage, 0, 0); // фон
        game.font.draw(game.batch, score + dropsGathered, 20, 470); // очки

        for (Rectangle live : lives){
            game.batch.draw(liveImage, live.x, live.y, Const.SIZE_IMAGE / 4, Const.SIZE_IMAGE / 4); // жизнь
        }

        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y); // капли
        }
        game.batch.draw(lightningBoltImage, lightningBolt.x, lightningBolt.y);
        game.batch.end();

        // <=> Увеличиваем скорость падения капель
        if (dropsGathered % 20 == 0 && dropsGathered != 0) dropsSpeed += indexSpeed; // через каждые пойманные до увеличиваем скорость падения капель

        // <=> Конец игры, если счет стал меньше -10
        if (dropsGathered < -10 || liveCount <= 0) game.setScreen(new GameOverScreen(game, dropsGathered));

        // <=> Слушатель нажатия на экран
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = (int) (touchPos.x - Const.SIZE_IMAGE / 2);
        }

        // <=> Слушатели нажатия кнопок клавиатуры: влево и вправо
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            bucket.x -= bucketSpeed * Gdx.graphics.getDeltaTime(); // Скорость дыижения ведра влево
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            bucket.x += bucketSpeed * Gdx.graphics.getDeltaTime(); // Скорость дыижения ведра вправо

        // <=> Ограничитель движения ведра по оси X
        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > Const.WIDTH_SCREEN - Const.SIZE_IMAGE) bucket.x = Const.WIDTH_SCREEN - Const.SIZE_IMAGE;

        // <=> Время для создания новой капли
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

        // <=> Время для молнии
        if (TimeUtils.millis() - lastTimeLightningBolt > 1000) {
            lightningBolt.x = Const.WIDTH_SCREEN;
            lightningBolt.y = Const.HEIGHT_SCREEN;
        }

        // Запускам капли в цикле
        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= dropsSpeed * Gdx.graphics.getDeltaTime(); // Скорость падения капли

            // <=> Удаление капли, если она ушла за пределы экрана по оси Y
            if (raindrop.y + Const.SIZE_IMAGE < 0) {
                iter.remove();
                // счет:
                dropsGathered -= 2; // -2, капля не поймана
                // координаты удара молнии
                rndLightningBoltsPath = MathUtils.random(0, 2);
                lightningBoltImage = new Texture(lightningBolts[rndLightningBoltsPath]);
                lightningBolt.x = raindrop.x - 290/2; // расчет удара молнии в пропущеную каплю
                lightningBolt.y = 0;
                lastTimeLightningBolt = TimeUtils.millis();
                // звук молнии
                loudlySound.play();
                // жизни--
                liveCount--;
                lives.removeIndex(liveCount);
            }

            // <=>  столкновение капли с ведром
            if (raindrop.overlaps(bucket)) {
                dropsGathered++; // счет. +1, капля поймана
                dropSound.play();//звук пойманой капли
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
        backgroundImage.dispose();
        bucketImage.dispose();
        dropImage.dispose();
        dropSound.dispose();
        loudlySound.dispose();
        rainMusic.dispose();
        lightningBoltImage.dispose();
        liveImage.dispose();
    }

    @Override
    public void show() {
        rainMusic.play();
    }
}
