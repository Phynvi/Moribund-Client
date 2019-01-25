package com.github.moribund.objects.nonplayable.projectile;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.github.moribund.MoribundClient;
import com.github.moribund.graphics.drawables.DrawableGameAsset;
import com.github.moribund.objects.attributes.Collidable;
import com.github.moribund.objects.attributes.Flaggable;
import com.github.moribund.objects.attributes.Movable;
import com.github.moribund.objects.flags.Flag;
import com.github.moribund.objects.flags.FlagConstants;
import com.github.moribund.objects.playable.players.PlayableCharacter;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import lombok.val;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A {@code Projectile} is not a singular {@link com.badlogic.gdx.graphics.g2d.Sprite},
 * rather it is any {@link com.badlogic.gdx.graphics.g2d.Sprite} that is moving
 * and visible in the screen that is not attached to a {@link com.badlogic.gdx.InputProcessor}.
 */
public class Projectile implements Movable, DrawableGameAsset, Flaggable {

    /**
     * The list of {@link Flag} for its {@link Flaggable} attribute.
     */
    private final ObjectSet<Flag> flags;

    /**
     * The list of {@link DrawableGameAsset} that don't count as "collisions" should they collide.
     */
    private final ObjectSet<DrawableGameAsset> ignores;

    /**
     * The sprite of the {@code Projectile}.
     */
    @Getter
    private final Sprite sprite;

    @Getter
    private final ProjectileType projectileType;
    @Getter
    private final PlayableCharacter source;
    /**
     * The speed at which the {@code Projectile} can rotate left or right.
     */
    private final float rotationSpeed;

    /**
     * The speed at which the {@code Projectile} can move forward or back.
     */
    private final float movementSpeed;
    private final LocalDateTime timeReleased;
    private final Polygon polygon;

    /**
     * Creates a new {@code Projectile} with a multitude of initial settings. It is important to note that in this
     * constructor, a {@code Projectile} is automatically marked with the {@link FlagConstants#MOVE_FORWARD_FLAG}
     * flag.
     */
    Projectile(ProjectileType projectileType, PlayableCharacter source, float startingX, float startingY, float startingAngle, float rotationSpeed, float movementSpeed, ObjectSet<DrawableGameAsset> ignores) {
        this.sprite = new Sprite(projectileType.getSprite());
        this.polygon = new Polygon(projectileType.getSpriteVertices().getVertices());
        this.projectileType = projectileType;
        this.source = source;
        this.rotationSpeed = rotationSpeed;
        this.movementSpeed = movementSpeed;
        this.ignores = new ObjectArraySet<>(ignores);
        flags = new ObjectArraySet<>();
        flags.add(FlagConstants.MOVE_FORWARD_FLAG);
        sprite.setX(startingX);
        sprite.setY(startingY);
        sprite.setRotation(startingAngle);
        polygon.setPosition(startingX, startingY);
        polygon.setRotation(startingAngle);
        polygon.setOrigin(sprite.getOriginX(), sprite.getOriginY());
        timeReleased = LocalDateTime.now();
    }

    /**
     * A helper method made for launching projectiles. Rather than making the person who wishes to launch a projectile
     * go through the hassle of adding the projectile to the list of {@link Flaggable} and {@link DrawableGameAsset} objects,
     * this helper method takes the projectile and does it for them.
     * @param projectile The respective {@code Projectile} that is about to be launched.
     */
    public static void launchProjectile(Projectile projectile) {
        MoribundClient.getInstance().getFlaggables().add(projectile);
        MoribundClient.getInstance().getDrawableGameAssets().add(projectile);
    }

    public void removeProjectile() {
        MoribundClient.getInstance().getFlaggables().remove(this);
        MoribundClient.getInstance().getDrawableGameAssets().remove(this);
    }

    /**
     * Calls the {@link ProjectileBuilder} as an API to construct a {@code Projectile}.
     * @return The newly created {@link ProjectileBuilder}.
     */
    public static ProjectileBuilder builder() {
        return new ProjectileBuilder();
    }

    @Override
    public void draw(Batch batch) {
        sprite.draw(batch);
        checkRemoval();
    }

    private void checkRemoval() {
        checkCollision();
        checkTimeExceeded();
    }

    private void checkTimeExceeded() {
        val timeTillRemoval = 3;
        val timeTillRemoveUnits = ChronoUnit.SECONDS;
        if (timeTillRemoveUnits.between(timeReleased, LocalDateTime.now()) >= timeTillRemoval) {
            removeProjectile();
        }
    }

    private void checkCollision() {
        MoribundClient.getInstance().getDrawableGameAssets().stream()
                .filter(drawable -> drawable instanceof Collidable)
                .filter(drawable -> !ignores.contains(drawable))
                .forEach(drawable -> {
                    Collidable collidable = (Collidable) drawable;
                    if (Intersector.overlapConvexPolygons(polygon, collidable.getPolygon())) {
                        collidable.collide(this);
                    }
                });
    }

    @Override
    public void processFlags() {
        flags.forEach(flag -> flag.processFlag(this));
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
        polygon.setPosition(x, getY());
    }

    @Override
    public void setY(float y) {
        sprite.setY(y);
        polygon.setPosition(getX(), y);
    }

    @Override
    public void setRotation(float angle) {
        sprite.setRotation(angle);
        polygon.setRotation(angle);
    }

    @Override
    public float getRotation() {
        return sprite.getRotation();
    }

    @Override
    public void rotateLeft() {
        sprite.rotate(-rotationSpeed);
    }

    @Override
    public void rotateRight() {
        sprite.rotate(rotationSpeed);
    }

    @Override
    public void moveForward() {
        val angle = sprite.getRotation();
        val xVelocity = movementSpeed * MathUtils.cosDeg(angle);
        val yVelocity = movementSpeed * MathUtils.sinDeg(angle);

        sprite.translate(xVelocity, yVelocity);
        polygon.translate(xVelocity, yVelocity);
    }

    @Override
    public void moveBack() {
        val angle = sprite.getRotation();
        val xVelocity = -movementSpeed * MathUtils.cosDeg(angle);
        val yVelocity = -movementSpeed * MathUtils.sinDeg(angle);

        sprite.translate(xVelocity, yVelocity);
        polygon.translate(xVelocity, yVelocity);
    }
}
