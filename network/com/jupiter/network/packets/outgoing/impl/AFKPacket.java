package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = -1, description = "Represents a Player's AFK state")
public class AFKPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		player.getSession().getChannel().close();
	}
}