package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.content.SkillCapeCustomizer;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = -1, description = "Represents a Color ID state")
public class ColorIDPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted())
			return;
		int colorId = stream.readUnsignedShort();
		if (player.getTemporaryAttributtes().get("SkillcapeCustomize") != null)
			SkillCapeCustomizer.handleSkillCapeCustomizerColor(player, colorId);
	}
}