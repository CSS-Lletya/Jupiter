package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.content.FriendChatsManager;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 71, description = "Represents a player joining a Friends Chat")
public class JoinFriendsChatPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted())
			return;
		int byte0 = stream.readUnsignedByte();
		String username = stream.readString();
		String supposed_username = Utility.getCharacterFromByte(byte0) + username;
		player.getPackets().sendGameMessage(String.format("byte0: %s, string: %s, fixedEnded: %s", byte0, username, supposed_username));
		FriendChatsManager.joinChat(supposed_username, player);
	}
}