package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = 84, description = "Represents a Player's AFK state")
public class ScreenPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		byte displayMode = (byte) stream.readUnsignedByte();
		player.setScreenWidth((short) stream.readUnsignedShort());
		player.setScreenHeight((short) stream.readUnsignedShort());
		@SuppressWarnings("unused")
		boolean switchScreenMode = stream.readUnsignedByte() == 1;
		if (!player.isStarted() || player.hasFinished() || displayMode == player.getDisplayMode() || !player.getInterfaceManager().containsInterface(742))
			return;
		player.setDisplayMode(displayMode);
		player.getInterfaceManager().removeAll();
		player.getInterfaceManager().sendInterfaces();
		player.getInterfaceManager().sendInterface(742);
	}
}