package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.plugin.handlers.NPCClickHandler;

@LogicPacketSignature(packetId = 80, packetSize = 95, description = "The Fourth menu option for a NPC")
public class NPCFourthClickPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		NPCClickHandler.executeMobInteraction(player, stream, 4);
	}
}