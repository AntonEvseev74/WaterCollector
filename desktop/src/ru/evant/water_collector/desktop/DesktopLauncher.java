package ru.evant.water_collector.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ru.evant.water_collector.Const;
import ru.evant.water_collector.Drop;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Water Collector";
		config.width = Const.WIDTH_SCREEN;
		config.height = Const.HEIGHT_SCREEN;
		config.fullscreen = false;
		config.resizable = false;
		new LwjglApplication(new Drop(), config);
	}
}
