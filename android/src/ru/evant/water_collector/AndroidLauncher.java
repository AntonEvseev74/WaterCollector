package ru.evant.water_collector;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;	// Отключить акселерометр
		config.useCompass = false;			// Отключить компас
		config.useImmersiveMode = true;		// Отключить нижние кнопки навигации
		initialize(new Drop(), config);
	}
}
