package com.jupiter.plugin.handlers;

import com.jupiter.plugin.events.NPCClickEvent;

public abstract class NPCClickHandler extends PluginHandler<NPCClickEvent> {
	private boolean checkDistance = true;
	
	public NPCClickHandler(boolean checkDistance, Object[] namesOrIds) {
		super(namesOrIds);
		this.checkDistance = checkDistance;
	}
	
	public NPCClickHandler(Object... namesOrIds) {
		super(namesOrIds);
	}
	
	public NPCClickHandler(String[] options) {
		super(options);
	}

	public boolean isCheckDistance() {
		return checkDistance;
	}
}
