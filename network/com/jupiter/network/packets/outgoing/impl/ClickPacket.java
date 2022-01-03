package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = 59, description = "Represents a Click-based state")
public class ClickPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		int mouseHash = stream.readShortLE128();
		int mouseButton = mouseHash >> 15;
		int time = mouseHash - (mouseButton << 15); // time
		int positionHash = stream.readIntV1();
		int y = positionHash >> 16; // y;
		int x = positionHash - (y << 16); // x
		@SuppressWarnings("unused")
		boolean clicked;
		// mass click or stupid autoclicker, lets stop lagg
		if (time <= 1 || x < 0 || x > player.getScreenWidth() || y < 0 || y > player.getScreenHeight()) {
			// player.getSession().getChannel().close();
			clicked = false;
			return;
		}
		clicked = true;
	}
}