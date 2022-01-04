package com.jupiter.plugins.rsinterface.impl;

import com.jupiter.game.player.Player;
import com.jupiter.plugins.rsinterface.RSInterface;
import com.jupiter.plugins.rsinterface.RSInterfaceSignature;

@RSInterfaceSignature(interfaceId = {271})
public class PrayerInterfacePlugin implements RSInterface {

	@Override
	public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
		if (componentId == 8 || componentId == 42)
			player.getPrayer().switchPrayer((byte) slotId);

		else if (componentId == 43 && player.getPrayer().settingQuickPrayers)
			player.getPrayer().switchSettingQuickPrayer();
	}
}