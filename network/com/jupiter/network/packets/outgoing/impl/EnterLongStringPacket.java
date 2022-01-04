package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.StringInputAction;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 87, description = "Represents a Input state")
public class EnterLongStringPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isActive() || player.isDead())
			return;
		int byte0 = stream.readUnsignedByte();
		String v1 = stream.readString();
		if (v1.equals(""))
			return;
		String value = Utility.getCharacterFromByte(byte0) + v1;
		if (player.getTemporaryAttributtes().get("string_input_action") != null) {
			StringInputAction action = (StringInputAction) player.getTemporaryAttributtes().remove("string_input_action");
			action.handle(value);
			return;
		}
		player.getPackets().sendGameMessage(""+value);
	}
}