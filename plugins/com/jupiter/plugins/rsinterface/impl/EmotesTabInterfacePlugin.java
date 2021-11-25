package com.jupiter.plugins.rsinterface.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.content.Emotes;
import com.jupiter.plugins.rsinterface.RSInterface;
import com.jupiter.plugins.rsinterface.RSInterfaceSignature;

@RSInterfaceSignature(interfaceId = {590})
public class EmotesTabInterfacePlugin implements RSInterface {

	@Override
	public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
		if ((interfaceId == 590 && componentId == 8) || interfaceId == 464) {
			Emotes.Emote.executeEmote(player, (byte) slotId);
		}
	}
}