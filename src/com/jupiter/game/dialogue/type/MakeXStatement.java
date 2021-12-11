package com.jupiter.game.dialogue.type;

import com.jupiter.cache.loaders.ItemDefinitions;
import com.jupiter.game.player.Player;

public class MakeXStatement implements Statement {
	
	public enum MakeXType {
		MAKE,
		MAKE_SET,
		COOK,
		ROAST,
		OFFER,
		SELL,
		BAKE,
		CUT,
		DEPOSIT,
		MAKE2,
		TELEPORT,
		SELECT,
		MAKE_SET2,
		TAKE,
		RETURN,
		HEAT,
		ADD
	}
	
	private MakeXType type;
	private int maxQuantity = -1;
	private String question;
	private int[] items;
	private String[] options;
	
	public MakeXStatement(MakeXType type, int maxQuantity, String question, int[] items, String[] options) {
		this.type = type;
		this.maxQuantity = 28;
		this.question = question;
		this.items = items;
		this.options = options;
	}
	
	public MakeXStatement(MakeXItem[] items, int maxQuantity) {
		this(MakeXType.MAKE, maxQuantity, "How many would you like to make?", null, null);
		int[] itemIds = new int[items.length];
		for (int i = 0;i < items.length;i++)
			itemIds[i] = items[i].getItemId();
		this.items = itemIds;
	}
	
	public MakeXStatement(int[] items, int maxQuantity) {
		this(MakeXType.MAKE, maxQuantity, "How many would you like to make?", items, null);
	}
	
	public MakeXStatement(int[] items, String[] options) {
		this(MakeXType.SELECT, -1, "Select an item.", items, options);
	}
	
	public MakeXStatement(int[] items) {
		this(MakeXType.SELECT, -1, "Select an item.", items, null);
	}

	@Override
	public void send(Player player) {
		player.getInterfaceManager().sendChatBoxInterface(905);
		player.getPackets().sendInterface(true, 905, 4, 916);
		player.getPackets().sendHideIComponent(916, 4, true);
		player.getPackets().sendHideIComponent(916, 9, true);
		for (int i = 15; i < 27; i++)
			player.getPackets().sendHideIComponent(916, i, true);
		player.getPackets().sendIComponentText(946, 6, question);
		player.getPackets().sendGlobalConfig(754, type.ordinal());
		for (int i = 0; i < 10; i++) {
			if (i >= items.length) {
				player.getPackets().sendGlobalConfig(i >= 6 ? (1139 + i - 6) : 755 + i, -1);
				continue;
			}
			player.getPackets().sendGlobalConfig(i >= 6 ? (1139 + i - 6) : 755 + i, items[i]);
			player.getPackets().sendGlobalString(i >= 6 ? (280 + i - 6) : 132 + i, options != null ? options[i] : ItemDefinitions.getItemDefinitions(items[i]).getName());
		}
		setMaxQuantity(player, maxQuantity);
		setQuantity(player, maxQuantity);
	}

	@Override
	public int getOptionId(int componentId) {
		if (componentId < 14)
			return 0;
		if (componentId == 26)
			return 7;
		if (componentId >= 21)
			return componentId - 13;
		return componentId - 14;
	}
	
	public static void setMaxQuantity(Player player, int maxQuantity) {
		player.getTemporaryAttributtes().put("SkillsDialogueMaxQuantity", maxQuantity);
		player.getPackets().sendConfigByFile(8094, maxQuantity);
	}

	public static void setQuantity(Player player, int quantity) {
		player.getTemporaryAttributtes().put("SkillsDialogueMaxQuantity", quantity);
		setQuantity(player, quantity, true);
	}

	public static void setQuantity(Player player, int quantity, boolean refresh) {
		int maxQuantity = getMaxQuantity(player);
		if (quantity > maxQuantity)
			quantity = maxQuantity;
		else if (quantity < 0)
			quantity = 0;
		if (refresh)
			player.getPackets().sendConfigByFile(8095, quantity);
	}

	public static int getMaxQuantity(Player player) {
		Integer maxQuantity = (Integer) player.getTemporaryAttributtes().get("SkillsDialogueMaxQuantity");
		if (maxQuantity == null)
			return 0;
		return maxQuantity;
	}

	public static int getQuantity(Player player) {
		Integer quantity = (Integer) player.getTemporaryAttributtes().get("SkillsDialogueQuantity");
		if (quantity == null)
			return 28;
		return quantity;
	}
}
