package com.jupiter.game.task.impl;

import com.jupiter.game.map.World;
import com.jupiter.game.task.Task;
import com.jupiter.skills.prayer.Prayer;

public final class DrainSkillsTask extends Task {
	
	/**
	 * Creates a new {@link DrainSkillsTask}.
	 */
	public DrainSkillsTask() {
		super(70, false);
	}
	
	@Override
	public void execute() {
		World.players().forEach(p -> {
			p.getPrayer();
			boolean usingBerserk = Prayer.usingBerserker(p);
			setDelay(!usingBerserk ? 70 : 96);
			p.drainSkills();
		});
	}
	
	@Override
	public void onCancel() {
		World.get().submit(new DrainSkillsTask());
	}
}