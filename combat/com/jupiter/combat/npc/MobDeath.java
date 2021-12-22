package com.jupiter.combat.npc;

import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.game.map.World;
import com.jupiter.game.task.Task;
import com.jupiter.game.task.impl.ActorDeathTask;
import com.jupiter.network.encoders.other.Animation;

/**
 * Represents a Mob (NPC) death event
 * @author Dennis
 *
 */
public class MobDeath extends ActorDeathTask<NPC> {

	/**
	 * Creates an instance of the NPC's combat definitions
	 */
	private final NPCCombatDefinitions definition = getActor().getCombatDefinitions();
	
	/**
	 * Constructs a NPC's death
	 * @param actor
	 */
	public MobDeath(NPC actor) {
		super(actor);
	}
	
	/**
	 * Handles pre death conditions
	 */
	@Override
	public void preDeath() {
		World.get().getTask().cancel(this);
		getActor().setNextAnimation(null);
		getActor().getPoisonDamage().set(0);
		getActor().resetWalkSteps();
		getActor().getCombat().removeTarget();
	}

	/**
	 * Handles death conditions
	 */
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

	/**
	 * Handles post death conditions
	 */
	@Override
	public void postDeath() {
		if (!getActor().isSpawned())
			getActor().setRespawnTask();
	}
}