package com.jupiter.plugins.listener;

import com.jupiter.game.item.Item;
import com.jupiter.game.player.Player;

/**
 * 
 * @author Dennis
 *
 */
public interface InventoryType {
	
	void execute(Player player, Item item, int option) throws Exception;
}
