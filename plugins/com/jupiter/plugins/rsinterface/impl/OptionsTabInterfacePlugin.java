package com.jupiter.plugins.rsinterface.impl;

import com.jupiter.game.player.Player;
import com.jupiter.plugins.rsinterface.RSInterface;
import com.jupiter.plugins.rsinterface.RSInterfaceSignature;

@RSInterfaceSignature(interfaceId = {261})
public class OptionsTabInterfacePlugin implements RSInterface {

	@Override
	public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
		if (player.getInterfaceManager().containsInventoryInter())
			return;
		if (componentId == 22) {
			if (player.getInterfaceManager().containsScreenInter()) {
				player.getPackets().sendGameMessage(
						"Please close the interface you have open before setting your graphic options.");
				return;
			}
			player.getAttributes().stopAll(player);
			player.getInterfaceManager().sendInterface(742);
		} else if (componentId == 12) {
			player.getPlayerDetails().setAllowChatEffects(player.getPlayerDetails().isAllowChatEffects());
			player.getPackets().sendConfig(171, player.getPlayerDetails().isAllowChatEffects() ? 0 : 1);
		} else if (componentId == 13) { // chat setup
			player.getInterfaceManager().sendSettings(982);
		} else if (componentId == 14) {
			player.getPlayerDetails().setMouseButtons(player.getPlayerDetails().isMouseButtons());
			player.getPackets().sendConfig(170, player.getPlayerDetails().isMouseButtons() ? 0 : 1);
		} else if (componentId == 24) // audio options
			player.getInterfaceManager().sendSettings(429);
		else if (componentId == 26)
			System.out.println("dead content");
	}
}