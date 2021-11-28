package com.jupiter.plugin.handlers;

import com.jupiter.game.map.WorldTile;
import com.jupiter.plugin.events.ItemOnObjectEvent;

public abstract class ItemOnObjectHandler extends PluginHandler<ItemOnObjectEvent> {
	
	private WorldTile[] tiles;
	private boolean checkDistance = true;

	public ItemOnObjectHandler(boolean checkDistance, Object[] namesOrIds, WorldTile... tiles) {
		super(namesOrIds);
		this.tiles = tiles;
		this.checkDistance = checkDistance;
	}
	
	public ItemOnObjectHandler(Object[] namesOrIds, WorldTile... tiles) {
		this(true, namesOrIds, tiles);
	}
	
	public ItemOnObjectHandler(Object[] namesOrIds) {
		this(true, namesOrIds);
	}

	public boolean isCheckDistance() {
		return checkDistance;
	}

	public WorldTile[] getTiles() {
		return tiles;
	}
}
