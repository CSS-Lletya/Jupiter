package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.plugin.handlers.ObjectClickHandler;

@LogicPacketSignature(packetId = 38, packetSize = 9, description = "The third menu option for a Object")
public class ObjectThirdClickPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		ObjectClickHandler.handleOption(player, stream, 3);
	}
}