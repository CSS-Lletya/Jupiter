package com.jupiter.game.dialogue.statment;

import com.jupiter.game.dialogue.Dialogue;
import com.jupiter.game.item.Item;
import com.jupiter.game.player.Player;

public class MakeXItem extends Dialogue {
	
	private int itemId;
		
	public MakeXItem(Player player, Item[] materials, Item[] products, double xp, int anim, int req, int skill, int delay) {
		this.itemId = products[0].getId();
		this.setFunc(() -> {
			int quantity = SkillsDialogue.getQuantity(player);
			for (Item mat : materials) {
				int newQ = player.getInventory().getNumberOf(mat.getId()) / mat.getAmount();
				if (newQ < quantity)
					quantity = newQ;
			}
			player.getActionManager().setAction(new CreateAction(new Item[][] { materials }, new Item[][] { products }, new double [] { xp }, new int[] { anim },  new int[] { req }, skill, delay, 0).setQuantity(quantity));
		});
	}
	
	public MakeXItem(Player player, Item material, Item product, double xp, int anim, int req, int skill, int delay) {
		this(player, new Item[] { material }, new Item[] { product }, xp, anim, req, skill, delay);
	}

	public int getItemId() {
		return itemId;
	}
}
