package com.jupiter.game.task.impl;

import com.jupiter.game.World;
import com.jupiter.game.task.Task;

/**
 * The class that handles the restoration special percentages.
 * @author Artem Batutin <artembatutin@gmail.com></artembatutin@gmail.com>
 */
public final class RestoreSpecialTask extends Task {
	
	/**
	 * Creates a new {@link RestoreSpecialTask}.
	 */
	public RestoreSpecialTask() {
		super(15, false);
	}
	
	@Override
	public void execute() {
		World.players().filter(p -> p.getCombatDefinitions().getSpecialAttackPercentage() < 100).forEach(p -> p.getCombatDefinitions().restoreSpecialAttack(5));
	}
	
	@Override
	public void onCancel() {
		World.get().submit(new RestoreSpecialTask());
	}
}
