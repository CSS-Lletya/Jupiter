package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.task.Task;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;
import com.jupiter.network.encoders.other.Hit;
import com.jupiter.network.encoders.other.Hit.HitLook;
import com.jupiter.utility.RandomUtility;

@MobCombatSignature(mobId = { 9463, 9465, 9467 }, mobName = {})
public class StrykewwyrmCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = RandomUtility.getRandom(10);
		if (attackStyle <= 7) { // melee
			int size = npc.getSize();
			int distanceX = target.getX() - npc.getX();
			int distanceY = target.getY() - npc.getY();
			if (distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1) {
				// nothing
			} else {
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 0, target,
						getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MAGE, target)));
				return defs.getAttackDelay();
			}
		}
		if (attackStyle <= 9) { // mage
			npc.setNextAnimation(new Animation(12794));
			final Hit hit = getMagicHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MAGE, target));
			delayHit(npc, 1, target, hit);
			World.sendProjectile(npc, target, defs.getAttackProjectile(), 41, 16, 41, 30, 16, 0);
			if (npc.getId() == 9463) {
				World.get().submit(new Task(1) {
					@Override
					protected void execute() {
						if (RandomUtility.getRandom(10) == 0 && target.getFreezeDelay() < System.currentTimeMillis()) {
							target.addFreezeDelay(3000);
							target.setNextGraphics(new Graphics(369));
							if (target instanceof Player) {
								Player targetPlayer = (Player) target;
								targetPlayer.getAttributes().stopAll(targetPlayer);
							}
						} else if (hit.getDamage() != 0)
							target.setNextGraphics(new Graphics(2315));
						this.cancel();
					}
				});
			}
		} else if (attackStyle == 10) { // bury
			final WorldTile tile = new WorldTile(target);
			tile.moveLocation(-1, -1, 0);
			npc.setNextAnimation(new Animation(12796));
			npc.setCantInteract(true);
			npc.getCombat().removeTarget();
			final int id = npc.getId();
			World.get().submit(new Task(1) {
				int count;
				@Override
				protected void execute() {
					if (count == 0) {

						npc.transformIntoNPC(id - 1);
						npc.setForceWalk(tile);
						count++;
					} else if (count == 1 && !npc.hasForceWalk()) {
						npc.transformIntoNPC(id);
						npc.setNextAnimation(new Animation(12795));
						int distanceX = target.getX() - npc.getX();
						int distanceY = target.getY() - npc.getY();
						int size = npc.getSize();
						if (distanceX < size && distanceX > -1 && distanceY < size && distanceY > -1)
							delayHit(npc, 0, target, new Hit(npc, 300, HitLook.REGULAR_DAMAGE));
						count++;
					} else if (count == 2) {
						npc.getCombat().setCombatDelay(defs.getAttackDelay());
						npc.setTarget(target);
						npc.setCantInteract(false);
						this.cancel();
					}
					this.cancel();
				}
			});
		}
		return defs.getAttackDelay();
	}
}