package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;

/**
 * This is a default class for mobs that have no special
 * combat abilities such as Man, Woman, Imp, such..
 * (Basically a fallback combat script if we don't have one made)
 * 
 * (use an array of NPC ids or call by array of npc names)
 * (Example: 708, 709, 710 | "imp", "Man", Woman")
 * @author Dennis
 *
 */
@MobCombatSignature(mobId = {}, mobName = {})
public class DefaultCombat extends MobCombatInterface {

	@Override
	public int execute(Player player, NPC npc) throws Exception {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = defs.getAttackStyle();
		if (attackStyle == NPCCombatDefinitions.MELEE) {
			delayHit(npc, 0, player, getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, player)));
		} else {
			int damage = getRandomMaxHit(npc, defs.getMaxHit(), attackStyle, player);
			delayHit(npc, 2, player,
					attackStyle == NPCCombatDefinitions.RANGE ? getRangeHit(npc, damage) : getMagicHit(npc, damage));
			if (defs.getAttackProjectile() != -1)
				World.sendProjectile(npc, player, defs.getAttackProjectile(), 41, 16, 41, 35, 16, 0);
		}
		if (defs.getAttackGfx() != -1)
			npc.setNextGraphics(new Graphics(defs.getAttackGfx()));
		npc.setNextAnimation(new Animation(defs.getAttackEmote()));
		return defs.getAttackDelay();
	}
}