package com.jupiter.plugin.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.plugin.handlers.ObjectClickHandler;
import com.jupiter.plugin.handlers.PluginHandler;

public class ObjectClickEvent implements PluginEvent {
	
	private static Map<Object, Map<Integer, List<ObjectClickHandler>>> METHODS = new HashMap<>();

	private Player player;
	private WorldObject object;
	private int optionId;
	private String option;
	
	public String getOptionString() {
		return object.getDefinitions().getOption(optionId);
	}
	
	public ObjectClickEvent(Player player, WorldObject object, int optionId) {
		this.player = player;
		this.object = object;
		this.optionId = optionId;
		this.option = object.getDefinitions().getOption(optionId);
	}
	
	public int getOptionId() {
		return optionId;
	}

	public Player getPlayer() {
		return player;
	}

	public WorldObject getObject() {
		return object;
	}
	
	public String getOption() {
		return option;
	}
	
	public boolean isAtObject() {
		return player.withinDistance(object, 1);
	}
	
	
	@Override
	public List<PluginHandler<? extends PluginEvent>> getMethods() {
		List<PluginHandler<? extends PluginEvent>> valids = new ArrayList<>();
		Map<Integer, List<ObjectClickHandler>> methodMapping = METHODS.get(getObjectId());
		if (methodMapping == null)
			methodMapping = METHODS.get(getObject().getDefinitions().getName());
		if (methodMapping == null)
			return null;
		List<ObjectClickHandler> methods = methodMapping.get(getObject().getTileHash());
		if (methods == null)
			methods = methodMapping.get(-getObject().getType());
		if (methods == null)
			methods = methodMapping.get(0);
		if (methods == null)
			return null;
		for (ObjectClickHandler method : methods) {
			if (!isAtObject() && method.isCheckDistance())
				continue;
			valids.add(method);
		}

		return valids;
	}

	public static void registerMethod(Class<?> eventType, PluginHandler<? extends PluginEvent> method) {
		ObjectClickHandler handler = (ObjectClickHandler) method;
		for (Object key : handler.keys()) {
			Map<Integer, List<ObjectClickHandler>> locMap = METHODS.get(key);
			if (locMap == null) {
				locMap = new HashMap<>();
				METHODS.put(key, locMap);
			}
			if (handler.getType() == null && (handler.getTiles() == null || handler.getTiles().length <= 0)) {
				List<ObjectClickHandler> methods = locMap.get(0);
				if (methods == null)
					methods = new ArrayList<>();
				methods.add(handler);
				locMap.put(0, methods);
			} else {
				if (handler.getType() != null) {
					List<ObjectClickHandler> methods = locMap.get(-handler.getType().id);
					if (methods == null)
						methods = new ArrayList<>();
					methods.add(handler);
					locMap.put(-handler.getType().id, methods);
				} else {
					for (WorldTile tile : handler.getTiles()) {
						List<ObjectClickHandler> methods = locMap.get(tile.getTileHash());
						if (methods == null)
							methods = new ArrayList<>();
						methods.add(handler);
						locMap.put(tile.getTileHash(), methods);
					}
				}
			}
		}
	}

	public boolean objectAt(int x, int y) {
		return object.isAt(x, y);
	}
	
	public boolean objectAt(int x, int y, int plane) {
		return object.isAt(x, y, plane);
	}

	public int getObjectId() {
		return object.getId();
	}
}
