package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.Entity;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.encoders.other.Graphics;
import com.jupiter.utils.Utils;

@MobCombatSignature(mobId = {6222}, mobName = {})
public class KreearaCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (!npc.isUnderCombat()) {
			npc.setNextAnimation(new Animation(6997));
			delayHit(npc, 1, target, getMeleeHit(npc, getRandomMaxHit(npc, 260, NPCCombatDefinitions.MELEE, target)));
			return defs.getAttackDelay();
		}
		npc.setNextAnimation(new Animation(6976));
		for (Entity t : npc.getPossibleTargets()) {
			if (Utils.getRandom(2) == 0)
				sendMagicAttack(npc, t);
			else {
				delayHit(npc, 1, t, getRangeHit(npc, getRandomMaxHit(npc, 720, NPCCombatDefinitions.RANGE, t)));
				World.sendProjectile(npc, t, 1197, 41, 16, 41, 35, 16, 0);
				WorldTile teleTile = t;
				for (int trycount = 0; trycount < 10; trycount++) {
					teleTile = new WorldTile(t, 2);
					if (World.canMoveNPC(t.getPlane(), teleTile.getX(), teleTile.getY(), t.getSize()))
						break;
				}
				t.setNextWorldTile(teleTile);
			}
		}
		return defs.getAttackDelay();
	}
	
	private void sendMagicAttack(NPC npc, Entity target) {
		npc.setNextAnimation(new Animation(6976));
		for (Entity t : npc.getPossibleTargets()) {
			delayHit(npc, 1, t, getMagicHit(npc, getRandomMaxHit(npc, 210, NPCCombatDefinitions.MAGE, t)));
			World.sendProjectile(npc, t, 1198, 41, 16, 41, 35, 16, 0);
			target.setNextGraphics(new Graphics(1196));
		}
	}
}