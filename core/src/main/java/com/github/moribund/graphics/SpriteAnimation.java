package com.github.moribund.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import lombok.Getter;
import lombok.val;

public class SpriteAnimation extends Animation<TextureRegion> {
    @Getter
    private float stateTime;
    private boolean playing;
    private Runnable whenEnded;

    SpriteAnimation(float frameDuration, Array<? extends TextureRegion> keyFrames) {
        super(frameDuration, keyFrames);
        playing = true;
    }

    public void drawAnimation(Batch batch, Sprite originalSprite) {
        stateTime += Gdx.graphics.getDeltaTime();
        val currentFrame = getKeyFrame(stateTime, false);

        batch.draw(currentFrame, originalSprite.getX(), originalSprite.getY(),
                originalSprite.getOriginX(), originalSprite.getOriginY(),
                currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
                originalSprite.getScaleX(), originalSprite.getScaleY(),
                originalSprite.getRotation());

        if (currentFrame == getKeyFrames()[getKeyFrames().length - 1]) {
            playing = false;
        }
    }

    public void whenEnded(Runnable runnable) {
        whenEnded = runnable;
    }

    public void end() {
        if (whenEnded != null) {
            whenEnded.run();
        }
        whenEnded = null;
        playing = true;
        stateTime = 0f;
    }

    public boolean isFinished() {
        return !playing;
    }
}