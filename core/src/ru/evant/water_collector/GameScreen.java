package ru.evant.water_collector;

/*
 * Логика игры:
 *
 * Нужно ведром ловить капли воды.
 * У игрока есть пять жизней (отображены в виде маленьких ведер в верхней части экрана).
 *
 * Если капли пойманы очки увеличиваются,
 * в ином случае: уменьшаются размеры капли и ведра, уменьшаются очки, гремит гром и бьёт молния.
 * После каждой пойманой 20й капли увеличивается скорость падения капель.
 *
 * Очки (отображены в верхнем правом углу экрана):
 * +1 -> Капля поймана
 * -2 -> Капля не поймана
 */

/*
 * - сделать экран рекордов
 * - сделать меню (кнопк: выход, рекорды)
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

    Texture backgroundImage;    // фон

    Sound dropSound;    // звук капли
    Sound loudlySound;  // звук грозы
    Music rainMusic;    // фоновый звук шума дождя

    Texture bucketImage;    // ведро
    Rectangle bucket;       // оболочка ведра (прямоугольный контейнер, в который поместим ведро, для работы с координатами и размерами ведра)

    Vector3 touchPos;       // для получения координат касания экрана мышкой(Click) или пальцем(Android),

    // Капли
    Texture dropImage;              // капля
    Array<Rectangle> raindrops;     // массив оболочек капли
    long lastDropTime;              // время создания капли

    int dropsGathered;              // Счетчик количества пойманых капель
    String score = "Score: ";       // Строка - Очки
    int dropsSpeed = 200;           // Начальная скорость капли
    int bucketSpeed = 200;          // Начальная скорость ведра
    int indexSpeed = 2;             // индекс для увеличения скорости падения капли (на сколько увеличить скорость)
    int respawnedDrop = 1000000000; // 1 сек в наносекундах
    int numberToUpSpeedDrop = 20;         // количество капель до увеличения скорости падения капли

    // Молния
    Texture lightningBoltImage;     // молния
    Rectangle lightningBolt;        // оболочка молнии
    String[] lightningBolts = {"lightning_bolts_1.png", "lightning_bolts_2.png", "lightning_bolts_3.png"}; // массив молний
    int rndLightningBoltsPath = MathUtils.random(0, 2); // случайное число от 0 до последнего индекса массива молний
    long lastTimeLightningBolt;                         // впремя создания молнии

    // Жизни
    Texture liveImage;      // жизнь
    Array<Rectangle> lives; // массив оболочек для жизней
    int liveCount = 5;      // количество жизней

    int sizeBucket = 64;    // размер ведра
    int sizeDrop = 64;      // размер капли

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
                    Const.HEIGHT_SCREEN - liveImage.getHeight()/4f - 10,
                    Const.SIZE_IMAGE/4f,
                    Const.SIZE_IMAGE/4f));
        }

        lightningBoltImage = new Texture(lightningBolts[rndLightningBoltsPath]);

        //Молния
        lightningBolt = new Rectangle();
        lightningBolt.x = Const.WIDTH_SCREEN / 2f - 300f / 2f;
        lightningBolt.y = Const.HEIGHT_SCREEN;

        //Включаем музыку
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        //ведро
        bucket = new Rectangle();
        bucket.x = Const.WIDTH_SCREEN / 2f - Const.SIZE_IMAGE / 2f;
        bucket.y = 20;
        bucket.width = Const.SIZE_IMAGE - 24; // (Размер для вычисления столкновения)
        bucket.height = Const.SIZE_IMAGE - 24; // (Размер для вычисления столкновения)

        //капля
        raindrops = new Array<>();
        spawnRaindrop();
    }

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, Const.WIDTH_SCREEN - Const.SIZE_IMAGE);    // координата х
		raindrop.y = Const.HEIGHT_SCREEN;                                           // координата у
		raindrop.width = Const.SIZE_IMAGE;                                          // размер длины
		raindrop.height = Const.SIZE_IMAGE;                                         // размер ширины
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();                                        // время создания в наносекундах
	}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined); // Установка матрицы экрана. Система координат (0;0) - нижний левый угол
        // > Отрисовка на экране Начало
        game.batch.begin();
        game.batch.draw(backgroundImage, 0, 0); // фон
        game.font.draw(game.batch, score + dropsGathered, 20, 470); // очки

        for (Rectangle live : lives){
            game.batch.draw(liveImage, live.x, live.y, Const.SIZE_IMAGE / 4f, Const.SIZE_IMAGE / 4f); // жизнь
        }

        game.batch.draw(bucketImage, bucket.x, bucket.y, sizeBucket, sizeBucket); // ведро
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y, sizeDrop, sizeDrop); // капли
        }

        game.batch.draw(lightningBoltImage, lightningBolt.x, lightningBolt.y);
        game.batch.end();
        // > Отрисовка на экране Конец

        // <=> Увеличиваем скорость падения капель
        if (dropsGathered % numberToUpSpeedDrop == 0 && dropsGathered != 0) {
            dropsSpeed += indexSpeed; // через каждые numberToUpDrop пойманные до увеличиваем скорость падения капель
            respawnedDrop -= indexSpeed*100; // Увеличиваем скорость появления капель
        }

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
        if (TimeUtils.nanoTime() - lastDropTime > respawnedDrop) {
            spawnRaindrop(); // создаем капли
        }

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
                lightningBolt.x = raindrop.x - 290/2f;       // расчет удара молнии в координаты пропущеной капли, координата х
                lightningBolt.y = 0;                        // координата у
                lastTimeLightningBolt = TimeUtils.millis(); // время создания молнии

                // звук молнии
                loudlySound.play();             // воспроизвести звук молнии
                // размеры ведра и капель
                sizeBucket -= 8;                // уменьшение размера ведра
                sizeDrop -= 8;                  // уменьшение размера капли
                // жизни--
                liveCount--;                    // уменьшаем количество жизней
                lives.removeIndex(liveCount);   // Удаляем из массива одну жизнь (элемент под номером - текущее значение liveCount)
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
