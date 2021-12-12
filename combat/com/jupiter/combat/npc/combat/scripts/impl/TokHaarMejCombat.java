package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.utility.RandomUtility;

@MobCombatSignature(mobId = {15203}, mobName = {})
public class TokHaarMejCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int distanceX = target.getX() - npc.getX();
		int distanceY = target.getY() - npc.getY();
		int size = npc.getSize();
		int hit = 0;
		int attackStyle = RandomUtility.random(2);
		if (attackStyle == 0 && (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)) {
			attackStyle = 1;
		}
		switch (attackStyle) {
		case 0:
			hit = getRandomMaxHit(npc, defs.getMaxHit() - 36, NPCCombatDefinitions.MELEE, target);
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target, getMeleeHit(npc, hit));
			break;
		case 1:
			hit = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MAGE, target);
			npc.setNextAnimation(new Animation(16122));
			World.sendProjectile(npc, target, 2991, 34, 16, 30, 35, 16, 0);
			delayHit(npc, 2, target, getMagicHit(npc, hit));
			break;
		}
		return defs.getAttackDelay();
	}
}