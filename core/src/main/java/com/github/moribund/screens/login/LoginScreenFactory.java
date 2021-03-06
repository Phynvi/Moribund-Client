package com.github.moribund.screens.login;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.moribund.audio.MusicPlayer;
import com.github.moribund.graphics.sprites.SpriteContainer;
import com.github.moribund.graphics.sprites.SpriteFile;
import lombok.val;

public class LoginScreenFactory {
    public Screen createScreen() {
        val musicPlayer = new MusicPlayer();
        val initialLoginScreenState = LoginScreenState.INPUT;
        val batch = new SpriteBatch();
        val background = new Sprite(SpriteContainer.getInstance().getSprite(SpriteFile.MINIMAP));
        val camera = createCamera();
        return new LoginScreen(musicPlayer, initialLoginScreenState, batch, background, camera);
    }

    private Camera createCamera() {
        val camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() * 3, Gdx.graphics.getHeight() * 4);
        return camera;
    }
}
