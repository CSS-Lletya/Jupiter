package com.jupiter.game.task.impl;

import com.jupiter.game.map.World;
import com.jupiter.game.task.Task;
import com.jupiter.skills.prayer.Prayer;

public final class RestoreSkillsTask extends Task {
	
	/**
	 * Creates a new {@link RestoreSkillsTask}.
	 */
	public RestoreSkillsTask() {
		super(60, false);
	}
	
	@Override
	public void execute() {
		World.players().forEach(p -> {
			boolean usingRapidRestore = p.getPrayer().active(Prayer.RAPID_RESTORE);
			setDelay(usingRapidRestore ? 12 : 24);
			p.getSkills().updateSkills(false);
		});
	}
	
	@Override
	public void onCancel() {
		World.get().submit(new RestoreSkillsTask());
	}
}