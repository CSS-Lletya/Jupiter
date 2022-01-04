package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = 30, description = "Represents a Chat type state")
public class ChatTypePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		player.getPlayerDetails().setChatType(stream.readUnsignedByte());
	}
}