package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.plugin.handlers.ObjectClickHandler;

@OutgoingPacketSignature(packetId = 73, description = "Represents a object examine event")
public class ObjectExaminePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		ObjectClickHandler.handleOption(player, stream, -1);
	}
}