package com.jupiter.plugin.handlers;

import com.jupiter.game.map.ObjectType;
import com.jupiter.game.map.WorldTile;
import com.jupiter.plugin.events.ObjectClickEvent;

public abstract class ObjectClickHandler extends PluginHandler<ObjectClickEvent> {
	
	private WorldTile[] tiles;
	private boolean checkDistance = true;
	private ObjectType type;

	public ObjectClickHandler(boolean checkDistance, Object[] namesOrIds, WorldTile... tiles) {
		super(namesOrIds);
		this.tiles = tiles;
		this.checkDistance = checkDistance;
	}
	
	public ObjectClickHandler(Object[] namesOrIds, WorldTile... tiles) {
		this(true, namesOrIds, tiles);
	}

	public ObjectClickHandler(Object[] namesOrIds) {
		this(true, namesOrIds);
	}
	
	public ObjectClickHandler(Object[] namesOrIds, ObjectType type) {
		this(true, namesOrIds);
		this.type = type;
	}

	public boolean isCheckDistance() {
		return checkDistance;
	}

	public WorldTile[] getTiles() {
		return tiles;
	}

	public ObjectType getType() {
		return type;
	}
}
