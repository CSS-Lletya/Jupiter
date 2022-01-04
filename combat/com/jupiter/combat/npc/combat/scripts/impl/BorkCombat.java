package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.ForceTalk;
import com.jupiter.utility.RandomUtility;

@MobCombatSignature(mobId = {}, mobName = {"Bork"})
public class BorkCombat extends MobCombatInterface {

	public boolean spawnOrk = false;
	
	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (npc.getHitpoints() <= (defs.getHitpoints() * 0.4) && !spawnOrk) {
//			Player player = (Player) target;
			npc.setNextForceTalk(new ForceTalk("Come to my aid, brothers!"));
//			player.getControlerManager().startControler("BorkControler", 1, npc);
			//TODO: add activity
			spawnOrk = true;
		}
		npc.setNextAnimation(new Animation(RandomUtility.getRandom(1) == 0 ? defs.getAttackEmote() : 8757));
		delayHit(npc, 0, target, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), -1, target)));
		return defs.getAttackDelay();
	}
}