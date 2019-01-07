package com.github.moribund.net.packets.account;

import com.github.moribund.net.packets.IncomingPacket;
import com.github.moribund.objects.playable.Player;
import com.github.moribund.utils.PlayerUtils;

/**
 * An instruction by the server to the client to draw a new
 * {@link Player} onto the screen.
 */
public final class DrawNewPlayerPacket implements IncomingPacket {
    private int gameId;
    /**
     * The {@link Player}'s unique ID.
     */
    private int playerId;
    /**
     * The x location of the new player.
     */
    private float x;
    /**
     * The y location of the new player.
     */
    private float y;
    /**
     * The angle of rotation of the new player.
     */
    private float rotation;

    /**
     * A private constructor to ensure the client cannot unexpectedly send this
     * request to the server.
     */
    private DrawNewPlayerPacket() { }

    @Override
    public void process() {
        PlayerUtils.makePlayer(gameId, playerId, x, y);
        PlayerUtils.rotatePlayer(playerId, rotation);
    }
}
