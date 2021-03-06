package com.jupiter.plugins.rsinterface.impl;

import com.jupiter.game.player.Player;
import com.jupiter.plugins.rsinterface.RSInterface;
import com.jupiter.plugins.rsinterface.RSInterfaceSignature;

/**
 * Mainly used to close interfaces that can't be closed properly.
 * @author Dennis
 *
 */

@RSInterfaceSignature(interfaceId = {429, 982, 1252})
public class CloseAnyInterfacePlugin implements RSInterface {

	@Override
	public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
		switch(interfaceId) {
		case 429:
			if (componentId == 18)
				player.getInterfaceManager().sendSettings();
			break;
		case 982:
			if (componentId == 5)
				player.getInterfaceManager().sendSettings();
			break;
		case 1252:
			if (componentId == 5)
				player.getInterfaceManager().closeFSOverlay();
			break;
		}
	}
}