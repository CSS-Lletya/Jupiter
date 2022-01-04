package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 80, description = "Represents a Input state")
public class EnterNamePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isActive() || player.isDead())
			return;
		int byte0 = stream.readUnsignedByte();
		String v1 = stream.readString();
		if (v1.equals(""))
			return;
		String value = Utility.getCharacterFromByte(byte0) + v1;
		
		if (player.getInterfaceManager().containsInterface(1108))
			player.getFriendsIgnores().setChatPrefix(value);
	}
}