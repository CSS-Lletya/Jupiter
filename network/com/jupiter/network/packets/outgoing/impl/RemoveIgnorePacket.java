package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 12, description = "Represents a remove ignore packet")
public class RemoveIgnorePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted())
			return;
		int byte0 = stream.readUnsignedByte();
		String username = stream.readString();
		String supposed_username = Utility.getCharacterFromByte(byte0) + username;
		//player.getPackets().sendGameMessage(String.format("byte0: %s, name: %s", unknownByte0, name));
		player.getFriendsIgnores().removeIgnore(supposed_username);
	}
}