package com.jupiter.game.item;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;

public class ItemConstants {

	public static boolean canWear(Item item, Player player) {
		if (player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			return true;
		// Any special conditions you want to apply return as false
		return true;
	}

	public static boolean isTradeable(Item item) {
		if (item.getDefinitions().isDestroyItem() || item.getDefinitions().isLended() || !item.getDefinitions().exchangableItem)
			return false;
		if (item.getDefinitions().getName().toLowerCase().contains("flaming skull"))
			return false;
		switch (item.getId()) {
		case 6570: // firecape
		case 6529: // tokkul
		case 7462: // barrow gloves
		case 23659: // tookhaar-kal
			return false;
		default:
			return true;
		}
	}
}