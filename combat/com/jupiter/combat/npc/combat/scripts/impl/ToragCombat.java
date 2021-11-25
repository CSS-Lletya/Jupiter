package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.player.Player;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.encoders.other.Graphics;
import com.jupiter.utils.Utils;

@MobCombatSignature(mobId = {}, mobName = {"Torag"})
public class ToragCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		int damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target);
		if (damage != 0 && target instanceof Player && Utils.random(3) == 0) {
			target.setNextGraphics(new Graphics(399));
			Player targetPlayer = (Player) target;
			targetPlayer.setRunEnergy(targetPlayer.getRunEnergy() > 4 ? targetPlayer.getRunEnergy() - 4 : 0);
		}
		delayHit(npc, 0, target, getMeleeHit(npc, damage));
		return defs.getAttackDelay();
	}
}