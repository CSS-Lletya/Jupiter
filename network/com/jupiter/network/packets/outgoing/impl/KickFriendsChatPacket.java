package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 91, description = "Represents a Kick friend chat event")
public class KickFriendsChatPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted())
			return;
		player.setLastPublicMessage(Utility.currentTimeMillis() + 1000); // avoids
		int byte0 = stream.readUnsignedByte();
		String username = stream.readString();
		String supposed_username = Utility.getCharacterFromByte(byte0) + username;
		player.getCurrentFriendChat().kickPlayerFromFriendsChannel(player, supposed_username);
	}
}