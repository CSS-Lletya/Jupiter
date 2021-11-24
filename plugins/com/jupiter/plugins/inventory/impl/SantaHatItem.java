package com.jupiter.plugins.inventory.impl;

import com.jupiter.game.item.Item;
import com.jupiter.game.player.Player;
import com.jupiter.plugins.listener.InventoryType;
import com.jupiter.plugins.wrapper.InventoryWrapper;

@InventoryWrapper(itemId = {1050})
public class SantaHatItem implements InventoryType {

	@Override
	public void execute(Player player, Item item, int option) throws Exception {
		System.out.println(option);
	}
}