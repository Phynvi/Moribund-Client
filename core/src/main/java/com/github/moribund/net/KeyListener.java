package com.github.moribund.net;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.github.moribund.MoribundClient;
import com.github.moribund.net.packets.key.KeyPressedResponsePacket;
import com.github.moribund.net.packets.key.KeyUnpressedResponsePacket;
import lombok.val;

/**
 * The {@code KeyListener} listens to all packets relating
 * to key presses.
 */
class KeyListener extends Listener {
    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof KeyPressedResponsePacket) {
            val keyPressedResponsePacket = (KeyPressedResponsePacket) object;
            val player = MoribundClient.getInstance().getPlayers().get(keyPressedResponsePacket.getPlayerId());
            player.keyPressed(keyPressedResponsePacket.getKeyPressed());
        } else if (object instanceof KeyUnpressedResponsePacket) {
            val keyUnpressedResponsePacket = (KeyUnpressedResponsePacket) object;
            val player = MoribundClient.getInstance().getPlayers().get(keyUnpressedResponsePacket.getPlayerId());
            player.keyUnpressed(keyUnpressedResponsePacket.getKeyUnpressed());
        }
    }
}
