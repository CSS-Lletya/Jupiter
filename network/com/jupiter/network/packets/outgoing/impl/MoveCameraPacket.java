package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = 83, description = "Represents a Camera movement state")
public class MoveCameraPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		// not using it atm
		stream.readUnsignedShort();
		stream.readUnsignedShort();
	}
}