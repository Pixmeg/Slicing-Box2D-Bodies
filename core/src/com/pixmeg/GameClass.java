package com.pixmeg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class GameClass extends Game {
	SpriteBatch batch;
	AssetManager manager;
	public BitmapFont font;


	@Override
	public void create () {
		batch = new SpriteBatch();
		manager = new AssetManager();
		manager.load("skin/uiskin.atlas", TextureAtlas.class);
		manager.finishLoading();

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Pacifico.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 22;
		font = generator.generateFont(parameter);


		/* Set your screen to DemoScreen for Demo version.*/

		setScreen(new DemoScreen(this));
		//setScreen(new MainScreen(this));
	}

	@Override
	public void dispose () {
		batch.dispose();
		manager.dispose();
		font.dispose();
	}
}
