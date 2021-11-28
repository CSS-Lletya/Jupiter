package com.jupiter.plugin.events;

import java.util.HashMap;
import java.util.Map;

import com.jupiter.game.item.Item;
import com.jupiter.game.player.Player;
import com.jupiter.plugin.handlers.PluginHandler;

public class ItemOnPlayerEvent implements PluginEvent {
	
	private static Map<Object, PluginHandler<? extends PluginEvent>> HANDLERS = new HashMap<>();

	private Player player;
	private Player otherPlayer;
	private Item item;

	public ItemOnPlayerEvent(Player player, Player otherPlayer, Item item) {
		this.player = player;
		this.otherPlayer = otherPlayer;
		this.item = item;
	}

	public Player getPlayer() {
		return player;
	}

	public Player getTarget() {
		return otherPlayer;
	}

	public Item getItem() {
		return item;
	}

	@Override
	public PluginHandler<? extends PluginEvent> getMethod() {
		PluginHandler<? extends PluginEvent> method = HANDLERS.get(item.getId());
		if (method == null)
			method = HANDLERS.get(item.getDefinitions().getName());
		if (method == null)
			return null;
		return method;
	}

	public static void registerMethod(Class<?> eventType, PluginHandler<? extends PluginEvent> method) {
		for (Object key : method.keys()) {
			PluginHandler<? extends PluginEvent> old = HANDLERS.put(key, method);
			if (old != null) {
				System.err.println("ERROR: Duplicate ItemOnPlayer methods for key: " + key);
			}
		}
	}
}
