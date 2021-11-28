package com.jupiter.plugin.events;

import java.util.HashMap;
import java.util.Map;

import com.jupiter.game.map.WorldObject;
import com.jupiter.game.player.Player;
import com.jupiter.plugin.handlers.InterfaceOnObjectHandler;
import com.jupiter.plugin.handlers.PluginHandler;

public class InterfaceOnObjectEvent implements PluginEvent {
	
	private static Map<Object, InterfaceOnObjectHandler> HANDLERS = new HashMap<>();

	private Player player;
	private int interfaceId;
	private int componentId;
	private int slotId;
	private WorldObject object;

	public InterfaceOnObjectEvent(Player player, WorldObject object, int interfaceId, int componentId, int slotId) {
		this.player = player;
		this.interfaceId = interfaceId;
		this.componentId = componentId;
		this.slotId = slotId;
		this.object = object;
	}

	public Player getPlayer() {
		return player;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public int getComponentId() {
		return componentId;
	}

	public int getSlotId() {
		return slotId;
	}

	public WorldObject getObject() {
		return object;
	}

	public int getObjectId() {
		return object.getId();
	}

	@Override
	public PluginHandler<? extends PluginEvent> getMethod() {
		InterfaceOnObjectHandler method = HANDLERS.get(interfaceId);
		if (method == null)
			method = HANDLERS.get(getInterfaceId());
		if (method == null)
			return null;
		return method;
	}

	public static void registerMethod(Class<?> eventType, PluginHandler<? extends PluginEvent> method) {
		for (Object key : method.keys()) {
			PluginHandler<? extends PluginEvent> old = HANDLERS.put(key, (InterfaceOnObjectHandler) method);
			if (old != null) {
				System.err.println("ERROR: Duplicate InterfaceOnObject methods for key: " + key);
			}
		}
	}
}
