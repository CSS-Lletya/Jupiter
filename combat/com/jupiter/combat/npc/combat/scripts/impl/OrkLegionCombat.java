package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.Animation;
import com.jupiter.game.ForceTalk;
import com.jupiter.game.player.Player;
import com.jupiter.utils.Utils;

@MobCombatSignature(mobId = {}, mobName = {"Ork legion"})
public class OrkLegionCombat extends MobCombatInterface {

	public String[] messages = { "For Bork!", "Die Human!", "To the attack!", "All together now!" };
	
	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		if (Utils.getRandom(3) == 0)
			npc.setNextForceTalk(new ForceTalk(messages[Utils.getRandom(messages.length > 3 ? 3 : 0)]));
		delayHit(npc, 0, target, getMeleeHit(npc, defs.getMaxHit()));
		return defs.getAttackDelay();
	}
}