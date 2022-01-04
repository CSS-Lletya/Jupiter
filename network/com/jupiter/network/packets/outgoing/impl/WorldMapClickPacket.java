package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = 5, description = "Represents a World map click")
public class WorldMapClickPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		int coordinateHash = stream.readIntLE();
		int x = coordinateHash >> 14;
		int y = coordinateHash & 0x3fff;
		int plane = coordinateHash >> 28;
		Integer hash = (Integer) player.getTemporaryAttributtes().get("worldHash");
		if (hash == null || coordinateHash != hash)
			player.getTemporaryAttributtes().put("worldHash", coordinateHash);
		else {
			player.getTemporaryAttributtes().remove("worldHash");
			player.getHintIconsManager().addHintIcon(x, y, plane, 20, 0, 2, -1, true);
			player.getPackets().sendConfig(1159, coordinateHash);
		}
	}
}