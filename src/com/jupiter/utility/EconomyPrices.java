package com.jupiter.utility;

import com.jupiter.cache.loaders.ItemDefinitions;
import com.jupiter.game.item.Item;
import com.jupiter.game.item.ItemConstants;

public final class EconomyPrices {

	public static int getPrice(int itemId) {
		ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
		if (defs.isNoted())
			itemId = defs.getCertId();
		else if (defs.isLended())
			itemId = defs.getLendId();
		if (!ItemConstants.isTradeable(new Item(itemId, 1)))
			return 0;
		if (itemId == 995) // TODO after here
			return 1;
		return defs.getValue() * 3; // TODO get price from real item from saved
									// prices from ge
	}

	private EconomyPrices() {

	}
}
