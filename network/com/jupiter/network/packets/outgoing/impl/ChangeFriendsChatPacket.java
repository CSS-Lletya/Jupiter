package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 7, description = "Represents a change of friends chat packet state")
public class ChangeFriendsChatPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted() || !player.getInterfaceManager().containsInterface(1108))
			return;
		int byte0 = stream.readUnsignedByte();
		int rank = stream.readUnsigned128Byte();
		String username = stream.readString();
		String supposed_username = Utility.getCharacterFromByte(byte0) + username;
		player.getFriendsIgnores().changeRank(supposed_username, rank);
	}
}