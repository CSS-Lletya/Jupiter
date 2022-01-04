package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 34, description = "Represents a player being added to Ignore list")
public class AddIgnorePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted())
			return;
		int byte0 = stream.readUnsignedByte();
		String username = stream.readString();
		boolean until_logout = stream.readUnsignedByte() == 1;
		//player.getPackets().sendGameMessage(String.format("byte0: %s, name: %s, logout_byte: %s", unknownByte0, name, task_boo));
		String supposed_username = Utility.getCharacterFromByte(byte0) + username;
		player.getFriendsIgnores().addIgnore(supposed_username, until_logout);
	}
}