package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;
import com.jupiter.utility.RandomUtility;

@MobCombatSignature(mobId = {}, mobName = {"Guthan"})
public class GuthanCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target);
		if (damage != 0 && RandomUtility.random(3) == 0) {
			target.setNextGraphics(new Graphics(398));
			npc.heal(damage);
		}
		delayHit(npc, 0, target, getMeleeHit(npc, damage));
		return defs.getAttackDelay();
	}
}