package com.jupiter.combat.npc.combat.scripts;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.player.CombatDefinitions;
import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.game.Entity;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.task.Task;
import com.jupiter.network.encoders.other.Hit;
import com.jupiter.network.encoders.other.Hit.HitLook;
import com.jupiter.skills.Skills;
import com.jupiter.utility.RandomUtility;

public abstract class MobCombatInterface {
	
	public int execute(Player target, NPC mob) throws Exception {
		return 0;
	}
	
	public static int getRandomMaxHit(NPC npc, int maxHit, int attackStyle, Entity target) {
		int[] bonuses = npc.getBonuses();
		double attack = bonuses == null ? 0
				: attackStyle == NPCCombatDefinitions.RANGE ? bonuses[CombatDefinitions.RANGE_ATTACK]
						: attackStyle == NPCCombatDefinitions.MAGE ? bonuses[CombatDefinitions.MAGIC_ATTACK]
								: bonuses[CombatDefinitions.STAB_ATTACK];
		double defence;
		if (target instanceof Player) {
			Player targetPlayer = (Player) target;
			defence = targetPlayer.getSkills().getLevel(Skills.DEFENCE)
					+ (2 * targetPlayer.getCombatDefinitions().getBonuses()[attackStyle == NPCCombatDefinitions.RANGE
							? CombatDefinitions.RANGE_DEF
							: attackStyle == NPCCombatDefinitions.MAGE ? CombatDefinitions.MAGIC_DEF
									: CombatDefinitions.STAB_DEF]);
			defence *= targetPlayer.getPrayer().getDefenceMultiplier();
		} else {
			NPC mobTarget = (NPC) target;
			defence = mobTarget.getBonuses() == null ? 0
					: mobTarget.getBonuses()[attackStyle == NPCCombatDefinitions.RANGE ? CombatDefinitions.RANGE_DEF
							: attackStyle == NPCCombatDefinitions.MAGE ? CombatDefinitions.MAGIC_DEF
									: CombatDefinitions.STAB_DEF];
			defence *= 2;
		}
		double probability = attack / defence;
		if (probability > 0.90) // max, 90% prob hit so even lvl 138 can miss at lvl 3
			probability = 0.90;
		else if (probability < 0.05) // minimun 5% so even lvl 3 can hit lvl 138
			probability = 0.05;
		if (probability < Math.random())
			return 0;
		return RandomUtility.random(maxHit);
	}
	
	public static Hit getRangeHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.RANGE_DAMAGE);
	}

	public static Hit getMagicHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.MAGIC_DAMAGE);
	}

	public static Hit getRegularHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.REGULAR_DAMAGE);
	}

	public static Hit getMeleeHit(NPC npc, int damage) {
		return new Hit(npc, damage, HitLook.MELEE_DAMAGE);
	}

	public static void delayHit(NPC npc, int delay, final Entity target, final Hit... hits) {
		npc.getCombat().addAttackedByDelay(target);
		World.get().submit(new Task(delay) {
			@Override
			protected void execute() {
				for (Hit hit : hits) {
					NPC npc = (NPC) hit.getSource();
					if (npc.isDead() || npc.hasFinished() || target.isDead() || target.hasFinished())
						return;
					target.applyHit(hit);
					npc.getCombat().doDefenceEmote(target);
					if (target instanceof Player) {
						Player targetPlayer = (Player) target;
						targetPlayer.getInterfaceManager().closeInterfaces();
						if (targetPlayer.getCombatDefinitions().isAutoRelatie() && !targetPlayer.getActionManager().hasSkillWorking()
								&& !targetPlayer.hasWalkSteps())
							targetPlayer.getActionManager().setAction(new PlayerCombat(npc));
					} else {
						NPC targetNPC = (NPC) target;
						if (!targetNPC.isUnderCombat() || targetNPC.canBeAttackedByAutoRelatie())
							targetNPC.setTarget(npc);
					}

				}
				this.cancel();
			}
		});
	}
}