package com.jupiter.combat.npc;

import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.game.Animation;
import com.jupiter.game.World;
import com.jupiter.game.task.Task;
import com.jupiter.game.task.impl.ActorDeathTask;

public class MobDeath extends ActorDeathTask<NPC> {

	private final NPCCombatDefinitions definition = getActor().getCombatDefinitions();
	
	public MobDeath(NPC actor) {
		super(actor);
	}
	
	@Override
	public void preDeath() {
		World.get().getTask().cancel(this);
		getActor().setNextAnimation(null);
		getActor().getPoisonDamage().set(0);
		getActor().resetWalkSteps();
		getActor().getCombat().removeTarget();
	}

	@Override
	public void death() {
		World.get().submit(new Task(0) {
			int loop;
			@Override
			protected void execute() {
				if (loop == 0) {
					getActor().setNextAnimation(new Animation(definition.getDeathEmote()));
				} else if (loop >= definition.getDeathDelay()) {
					getActor().drop();
					getActor().reset();
					getActor().setLocation(getActor().getRespawnTile());
					getActor().finish();
					this.cancel();
				}
				loop++;
			}
		});
	}

	@Override
	public void postDeath() {
		if (!getActor().isSpawned())
			getActor().setRespawnTask();
	}
}