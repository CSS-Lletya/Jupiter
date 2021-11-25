package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.task.Task;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.encoders.other.Graphics;
import com.jupiter.utils.Utils;

/**
 * Non melee doesn't hit, find out why.
 * 
 * @author Dennis
 *
 */
@MobCombatSignature(mobId = {}, mobName = { "Jad" })
public class JadCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = Utils.random(3);
		if (attackStyle == 2) { // melee
			int distanceX = target.getX() - npc.getX();
			int distanceY = target.getY() - npc.getY();
			int size = npc.getSize();
			if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)
				attackStyle = Utils.random(2); // set mage
			else {
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 1, target,
						getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
				return defs.getAttackDelay();
			}
		}
		if (attackStyle == 1) { // range
			npc.setNextAnimation(new Animation(16202));
			npc.setNextGraphics(new Graphics(2994));
			World.get().submit(new Task(3) {
				@Override
				protected void execute() {
					target.setNextGraphics(new Graphics(3000));
					delayHit(npc, 1, target, getRangeHit(npc,
							getRandomMaxHit(npc, defs.getMaxHit() - 2, NPCCombatDefinitions.RANGE, target)));
					this.cancel();
				}
			});
		} else {
			npc.setNextAnimation(new Animation(16195));
			npc.setNextGraphics(new Graphics(2995));
			World.get().submit(new Task(2) {
				@Override
				protected void execute() {
					target.setNextGraphics(new Graphics(2741, 0, 100));
					delayHit(npc, 0, target, getMagicHit(npc,
							getRandomMaxHit(npc, defs.getMaxHit() - 2, NPCCombatDefinitions.MAGE, target)));
					this.cancel();
				}
			});
		}

		return defs.getAttackDelay() + 2;
	}
}