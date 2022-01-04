package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.plugin.handlers.ObjectClickHandler;

@LogicPacketSignature(packetId = 48, packetSize = 9, description = "The fifth menu option for a Object")
public class ObjectFifthClickPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		ObjectClickHandler.handleOption(player, stream, 5);
	}
}