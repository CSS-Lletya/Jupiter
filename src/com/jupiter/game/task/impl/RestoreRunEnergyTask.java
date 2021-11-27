package com.jupiter.game.task.impl;

import com.jupiter.game.map.World;
import com.jupiter.game.task.Task;
import com.jupiter.skills.Skills;

public final class RestoreRunEnergyTask extends Task {

	/**
	 * Creates a new {@link RestoreRunEnergyTask}.
	 */
	public RestoreRunEnergyTask() {
		super(2, true);
	}

	@Override
	public void execute() {
		World.players().filter(p -> p.getPlayerDetails().getRunEnergy() < 100 && (p.getWalkSteps().isEmpty())).forEach(p -> {
			double restoreRate = 0.45D;
			double agilityFactor = 0.01 * p.getSkills().getLevel(Skills.AGILITY);
			p.getMovement().setRunEnergy(p.getPlayerDetails().getRunEnergy() + (restoreRate + agilityFactor));
			p.getPackets().sendRunEnergy();
		});
	}

	@Override
	public void onCancel() {
		World.get().submit(new RestoreRunEnergyTask());
	}
}