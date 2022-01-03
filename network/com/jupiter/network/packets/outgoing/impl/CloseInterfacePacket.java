package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = 60, description = "Represents a Interface closing packet")
public class CloseInterfacePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (player.isStarted() && !player.hasFinished() && !player.isActive()) { // used for old welcome screen
			player.run();
			return;
		}
		player.getAttributes().stopAll(player);
	}
}