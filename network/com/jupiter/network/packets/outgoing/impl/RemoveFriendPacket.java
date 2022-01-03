package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 29, description = "Represents a playing being removed from friends list")
public class RemoveFriendPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted())
			return;
		int byte0 = stream.readUnsignedByte();
		String username = stream.readString();
		String supposed_username = Utility.getCharacterFromByte(byte0) + username;
		player.getFriendsIgnores().removeFriend(supposed_username);
	}
}