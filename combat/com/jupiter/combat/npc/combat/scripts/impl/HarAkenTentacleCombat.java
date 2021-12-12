package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.utility.RandomUtility;

@MobCombatSignature(mobId = {15209, 15210}, mobName = {})
public class HarAkenTentacleCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int distanceX = target.getX() - npc.getX();
		int distanceY = target.getY() - npc.getY();
		int size = npc.getSize();
		int attackStyle = RandomUtility.random(2);
		if (attackStyle == 0 && (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)) {
			attackStyle = 1;
		}
		switch (attackStyle) {
		case 0:
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target,
					getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit() - 36, NPCCombatDefinitions.MELEE, target)));
			break;
		case 1:
			npc.setNextAnimation(new Animation(npc.getId() == 15209 ? 16253 : 16242));
			World.sendProjectile(npc, target, npc.getId() == 15209 ? 3004 : 2922, 140, 35, 80, 35, 16, 0);
			if (npc.getId() == 15209)
				delayHit(npc, 2, target,
						getRangeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.RANGE, target)));
			else
				delayHit(npc, 2, target,
						getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
			break;
		}
		return defs.getAttackDelay();
	}
}