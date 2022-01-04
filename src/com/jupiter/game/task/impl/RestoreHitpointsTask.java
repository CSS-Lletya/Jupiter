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
			boolean usingRenewal = p.getPrayer().active(Prayer.RAPID_RENEWAL);
			boolean usingRapidHeal = p.getPrayer().active(Prayer.RAPID_HEAL);
			setDelay((usingRenewal ? 15 : p.isResting() ? 10 : usingRapidHeal ? 15 : 30));
			p.restoreHitPoints();
		});
	}
	
	@Override
	public void onCancel() {
		World.get().submit(new RestoreHitpointsTask());
	}
}