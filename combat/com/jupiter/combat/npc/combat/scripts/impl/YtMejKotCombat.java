package com.jupiter.combat.npc.combat.scripts.impl;

import java.util.List;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.encoders.other.Graphics;

@MobCombatSignature(mobId = {}, mobName = {"Yt-MejKot"})
public class YtMejKotCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		delayHit(npc, 0, target,
				getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), defs.getAttackStyle(), target)));
		if (npc.getHitpoints() < npc.getMaxHitpoints() / 2) {
			if (npc.getTemporaryAttributtes().remove("Heal") != null) {
				npc.setNextGraphics(new Graphics(2980, 0, 100));
				List<Integer> npcIndexes = World.getRegion(npc.getRegionId()).getNPCsIndexes();
				if (npcIndexes != null) {
					for (int npcIndex : npcIndexes) {
						NPC n = World.getNPCs().get(npcIndex);
						if (n == null || n.isDead() || n.hasFinished())
							continue;
						n.heal(100);
					}
				}
			} else
				npc.getTemporaryAttributtes().put("Heal", Boolean.TRUE);
		}
		return defs.getAttackDelay();
	}
}