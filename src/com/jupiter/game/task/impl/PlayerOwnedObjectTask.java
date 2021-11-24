package com.jupiter.game.task.impl;

import com.jupiter.game.World;
import com.jupiter.game.player.OwnedObjectManager;
import com.jupiter.game.task.Task;

public final class PlayerOwnedObjectTask extends Task {
	
	/**
	 * Creates a new {@link PlayerOwnedObjectTask}.
	 */
	public PlayerOwnedObjectTask() {
		super(1, false);
	}
	
	@Override
	public void execute() {
		World.players().filter(p -> !p.getOwnedObjectManagerKeys().isEmpty()).forEach(p -> OwnedObjectManager.processAll());
	}
	
	@Override
	public void onCancel() {
		World.get().submit(new PlayerOwnedObjectTask());
	}
}