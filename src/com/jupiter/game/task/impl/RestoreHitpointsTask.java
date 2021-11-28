package com.jupiter.game.task.impl;

import com.jupiter.game.map.World;
import com.jupiter.game.task.Task;
import com.jupiter.skills.prayer.Prayer;

public final class RestoreHitpointsTask extends Task {
	
	/**
	 * Creates a new {@link RestoreHitpointsTask}.
	 */
	public RestoreHitpointsTask() {
		super(30, false);
	}
	
	@Override
	public void execute() {
		World.players().forEach(p -> {
			boolean usingRenewal = Prayer.usingRapidRenewal(p);
			boolean usingRapidHeal = Prayer.usingRapidHeal(p);
			setDelay((usingRenewal ? 15 : p.getMovement().isResting() ? 10 : usingRapidHeal ? 15 : 30));
			p.restoreHitPoints();
		});
	}
	
	@Override
	public void onCancel() {
		World.get().submit(new RestoreHitpointsTask());
	}
}