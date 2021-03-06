package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;
import com.jupiter.utility.RandomUtility;

@MobCombatSignature(mobId = {}, mobName = { "TokHaar-Ket-Dill" })
public class TokHaarKetDillCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (RandomUtility.random(6) == 0) {
			delayHit(npc, 0, target, getRegularHit(npc, RandomUtility.random(defs.getMaxHit() + 1)));
			target.setNextGraphics(new Graphics(2999));
			if (target instanceof Player) {
				Player playerTarget = (Player) target;
				playerTarget.getPackets().sendGameMessage("The TokHaar-Ket-Dill slams it's tail to the ground.");
			}
		} else {
			delayHit(npc, 0, target,
					getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), defs.getAttackStyle(), target)));
		}
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		return defs.getAttackDelay();
	}
}