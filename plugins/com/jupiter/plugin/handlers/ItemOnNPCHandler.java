package com.jupiter.plugin.handlers;

import com.jupiter.plugin.events.ItemOnNPCEvent;

public abstract class ItemOnNPCHandler extends PluginHandler<ItemOnNPCEvent> {
	private boolean checkDistance = true;
	
	public ItemOnNPCHandler(boolean checkDistance, Object[] namesOrIds) {
		super(namesOrIds);
		this.checkDistance = checkDistance;
	}
	
	public ItemOnNPCHandler(Object... namesOrIds) {
		super(namesOrIds);
	}

	public boolean isCheckDistance() {
		return checkDistance;
	}
}
