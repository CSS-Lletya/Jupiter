package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.plugin.handlers.NPCClickHandler;

@LogicPacketSignature(packetId = 727, packetSize = 3, description = "The Third menu option for a NPC")
public class NPCThirdClickPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		NPCClickHandler.executeMobInteraction(player, stream, 3);
	}
}