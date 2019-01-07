package com.github.moribund.objects.playable;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.github.moribund.MoribundClient;
import com.github.moribund.images.SpriteContainer;
import com.github.moribund.images.SpriteFile;
import com.github.moribund.net.packets.key.KeyPressedPacket;
import com.github.moribund.net.packets.key.KeyUnpressedPacket;
import com.github.moribund.objects.flags.Flag;
import com.github.moribund.objects.flags.FlagConstants;
import com.github.moribund.objects.nonplayable.projectile.Projectile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import lombok.val;

/**
 * The {@code Player} that is being controlled by a client. The {@code Player}
 * is a type of {@link InputProcessor} for it is bound to {@link Player#keyBinds}.
 */
public class Player extends PlayableCharacter {

    private static final int ROTATION_SPEED = 5;
    private static final int MOVEMENT_SPEED = 5;

    @Getter
    private final int gameId;
    /**
     * The unique player ID based on the {@link com.esotericsoftware.kryonet.Connection} of
     * the client to the server.
     */
    @Getter
    private final int playerId;
    /**
     * The {@link Sprite} of this {@code Player} that represents the {@code Player}
     * in the live game visually.
     */
    @Getter
    private Sprite sprite;
    /**
     * The respective {@link com.badlogic.gdx.Input.Keys} that are bound to
     * {@link Runnable} methods defined in this class.
     */
    private Int2ObjectMap<PlayerAction> keyBinds;
    /**
     * The currently active {@link Flag}s on the {@code Player}.
     */
    private final ObjectSet<Flag> flags;
    /**
     * The currently active {@link Flag}s on the {@code Player} that will soon be removed. This is cleared whenever
     * the respective {@link Player#flags} have been removed from their {@link ObjectSet}.
     */
    private final ObjectSet<Flag> flagsToRemove;

    /**
     * Makes a {@code Player} with its unique player ID generated by
     * the {@link com.esotericsoftware.kryonet.Connection} between the
     * client and the server.
     * @param playerId The unique player ID.
     */
    public Player(int gameId, int playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
        sprite = new Sprite(SpriteContainer.getInstance().getSprite(SpriteFile.PLAYER));
        flags = new ObjectArraySet<>();
        flagsToRemove = new ObjectArraySet<>();
    }

    /**
     * Flags a new {@link Flag} on the player.
     * @param flag The {@link Flag} to flag.
     */
    private void flag(Flag flag) {
        flags.add(flag);
    }

    /**
     * Removes a {@link Flag} on the player.
     * @param flag The {@link Flag} that is not longer active.
     */
    private void flagToRemove(Flag flag) {
        flagsToRemove.add(flag);
    }

    @Override
    public void processFlags() {
        flags.removeAll(flagsToRemove);
        flagsToRemove.clear();
        flags.forEach(flag -> flag.processFlag(this));
    }

    @Override
    public void setRotation(float angle) {
        sprite.setRotation(angle);
    }

    @Override
    public float getRotation() {
        return sprite.getRotation();
    }

    @Override
    public void bindKeys() {
        keyBinds.put(Input.Keys.UP, new PlayerAction() {
            @Override
            public void keyPressed() {
                flag(FlagConstants.MOVE_FORWARD_FLAG);
            }

            @Override
            public void keyUnpressed() {
                flagToRemove(FlagConstants.MOVE_FORWARD_FLAG);
            }
        });
        keyBinds.put(Input.Keys.DOWN, new PlayerAction() {
            @Override
            public void keyPressed() {
                flag(FlagConstants.MOVE_BACKWARD_FLAG);
            }

            @Override
            public void keyUnpressed() {
                flagToRemove(FlagConstants.MOVE_BACKWARD_FLAG);
            }
        });
        keyBinds.put(Input.Keys.RIGHT, new PlayerAction() {
            @Override
            public void keyPressed() {
                flag(FlagConstants.ROTATE_RIGHT_FLAG);
            }

            @Override
            public void keyUnpressed() {
                flagToRemove(FlagConstants.ROTATE_RIGHT_FLAG);
            }
        });
        keyBinds.put(Input.Keys.LEFT, new PlayerAction() {
            @Override
            public void keyPressed() {
                flag(FlagConstants.ROTATE_LEFT_FLAG);
            }

            @Override
            public void keyUnpressed() {
                flagToRemove(FlagConstants.ROTATE_LEFT_FLAG);
            }
        });
        keyBinds.put(Input.Keys.B, new PlayerAction() {
            @Override
            public void keyPressed() {
                val newSprite = new Sprite(SpriteContainer.getInstance().getSprite(SpriteFile.PLAYER_WITH_BOW));
                newSprite.setPosition(sprite.getX(), sprite.getY());
                newSprite.setRotation(sprite.getRotation());
                sprite = newSprite;
            }

            @Override
            public void keyUnpressed() {

            }
        });
        keyBinds.put(Input.Keys.T, new PlayerAction() {
            @Override
            public void keyPressed() {
                val projectile = Projectile.builder()
                        .withSprite(SpriteFile.ARROW_PROJECTILE)
                        .withMovementSpeed(10)
                        .withAngle(getRotation())
                        .atXY(getX(), getY())
                        .create();
                Projectile.launchProjectile(projectile);
            }

            @Override
            public void keyUnpressed() {

            }
        });
    }

    @Override
    public float getX() {
        return sprite.getX();
    }

    @Override
    public float getY() {
        return sprite.getY();
    }

    @Override
    public void setX(float x) {
        sprite.setX(x);
    }

    @Override
    public void setY(float y) {
        sprite.setY(y);
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        sprite.draw(spriteBatch);
    }

    @Override
    public void rotateLeft() {
        sprite.rotate(ROTATION_SPEED);
    }

    @Override
    public void rotateRight() {
        sprite.rotate(-ROTATION_SPEED);
    }

    @Override
    public void moveForward() {
        val angle = sprite.getRotation();
        val xVelocity = getXVelocity(false, angle);
        val yVelocity = getYVelocity(false, angle);

        sprite.translate(xVelocity, yVelocity);
    }

    @Override
    public void moveBack() {
        val angle = sprite.getRotation();
        val xVelocity = getXVelocity(true, angle);
        val yVelocity = getYVelocity(true, angle);

        sprite.translate(xVelocity, yVelocity);
    }

    private float getXVelocity(boolean back, float angle) {
        val xVelocity = (back ? -1 : 1) * MOVEMENT_SPEED * MathUtils.cosDeg(angle);
        val xBound = SpriteContainer.getInstance().getSprite(SpriteFile.BACKGROUND).getWidth() / 2;
        return getVelocityWithLimitations(xVelocity, getX(), xBound, -xBound);
    }

    private float getYVelocity(boolean back, float angle) {
        val yVelocity = (back ? -1 : 1) * MOVEMENT_SPEED * MathUtils.sinDeg(angle);
        val yBound = SpriteContainer.getInstance().getSprite(SpriteFile.BACKGROUND).getHeight() / 2;
        return getVelocityWithLimitations(yVelocity, getY(), yBound, -yBound);
    }

    private float getVelocityWithLimitations(float velocity, float dependentVariable, float upperBound, float lowerBound) {
        val balancingConstant = 50;
        if (velocity < 0 && dependentVariable <= lowerBound) {
            velocity = 0;
        } else if (velocity > 0 && dependentVariable >= upperBound - balancingConstant) {
            velocity = 0;
        }
        return velocity;
    }

    @Override
    public Int2ObjectMap<PlayerAction> getKeyBinds() {
        if (keyBinds == null) {
            keyBinds = new Int2ObjectOpenHashMap<>();
            bindKeys();
        }
        return keyBinds;
    }

    @Override
    public void keyPressed(int keyPressed) {
        getKeyBinds().get(keyPressed).keyPressed();
    }

    @Override
    public void keyUnpressed(int keyUnpressed) {
        getKeyBinds().get(keyUnpressed).keyUnpressed();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (getKeyBinds().containsKey(keycode)) {
            val player = MoribundClient.getInstance().getPlayer();
            val packetDispatcher = MoribundClient.getInstance().getPacketDispatcher();
            val keyPressedPacket = new KeyPressedPacket(player.getGameId(), player.getPlayerId(), keycode);
            packetDispatcher.sendUDP(keyPressedPacket);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (getKeyBinds().containsKey(keycode)) {
            val player = MoribundClient.getInstance().getPlayer();
            val packetDispatcher = MoribundClient.getInstance().getPacketDispatcher();
            val keyUnpressedPacket = new KeyUnpressedPacket(player.getGameId(), player.getPlayerId(), keycode);
            packetDispatcher.sendUDP(keyUnpressedPacket);
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}