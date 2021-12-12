package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.combat.player.Combat;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;
import com.jupiter.utility.RandomUtility;

@MobCombatSignature(mobId = {}, mobName = {"Frost Dragon"})
public class FrostDragonsCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		final Player player = target instanceof Player ? (Player) target : null;
		int damage;
		switch (RandomUtility.getRandom(3)) {
		case 0: // Melee
			if (npc.withinDistance(target, 3)) {
				damage = getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target);
				npc.setNextAnimation(new Animation(defs.getAttackEmote()));
				delayHit(npc, 0, target, getMeleeHit(npc, damage));
			} else {
				damage = RandomUtility.getRandom(650);
				if (Combat.hasAntiDragProtection(target) || (player != null
						&& (player.getPrayer().usingPrayer(0, 17) || player.getPrayer().usingPrayer(1, 7)))) {
					damage = 0;
					player.getPackets()
							.sendGameMessage("Your " + (Combat.hasAntiDragProtection(target) ? "shield" : "prayer")
									+ " absorbs most of the dragon's breath!", true);
				} else if ((!Combat.hasAntiDragProtection(target) || !player.getPrayer().usingPrayer(0, 17)
						|| !player.getPrayer().usingPrayer(1, 7))
				&& player.getAntifireDetails().isPresent()) {
					damage = RandomUtility.getRandom(164);
					player.getPackets().sendGameMessage("Your potion absorbs most of the dragon's breath!", true);
				}
				npc.setNextAnimation(new Animation(13155));
				World.sendProjectile(npc, target, 393, 28, 16, 35, 35, 16, 0);
				delayHit(npc, 1, target, getRegularHit(npc, damage));
			}
			break;
		case 1: // Dragon breath
			if (npc.withinDistance(target, 3)) {
				damage = RandomUtility.getRandom(650);
				if (Combat.hasAntiDragProtection(target) || (player != null
						&& (player.getPrayer().usingPrayer(0, 17) || player.getPrayer().usingPrayer(1, 7)))) {
					damage = 0;
					player.getPackets()
							.sendGameMessage("Your " + (Combat.hasAntiDragProtection(target) ? "shield" : "prayer")
									+ " absorbs most of the dragon's breath!", true);
				} else if ((!Combat.hasAntiDragProtection(target) || !player.getPrayer().usingPrayer(0, 17)
						|| !player.getPrayer().usingPrayer(1, 7))
				/* && player.getFireImmune() > Utils.currentTimeMillis() */) {
					damage = RandomUtility.getRandom(164);
					player.getPackets().sendGameMessage(
							"Your potion fully protects you from the heat of the dragon's breath!", true);
				}
				npc.setNextAnimation(new Animation(13152));
				npc.setNextGraphics(new Graphics(2465));
				delayHit(npc, 1, target, getRegularHit(npc, damage));
			} else {
				damage = RandomUtility.getRandom(650);
				if (Combat.hasAntiDragProtection(target) || (player != null
						&& (player.getPrayer().usingPrayer(0, 17) || player.getPrayer().usingPrayer(1, 7)))) {
					damage = 0;
					player.getPackets()
							.sendGameMessage("Your " + (Combat.hasAntiDragProtection(target) ? "shield" : "prayer")
									+ " absorbs most of the dragon's breath!", true);
				} else if ((!Combat.hasAntiDragProtection(target) || !player.getPrayer().usingPrayer(0, 17)
						|| !player.getPrayer().usingPrayer(1, 7))
						&& player.getAntifireDetails().isPresent()) {
					damage = RandomUtility.getRandom(164);
					player.getPackets().sendGameMessage(
							"Your potion fully protects you from the heat of the dragon's breath!", true);
				}
				npc.setNextAnimation(new Animation(13155));
				World.sendProjectile(npc, target, 393, 28, 16, 35, 35, 16, 0);
				delayHit(npc, 1, target, getRegularHit(npc, damage));
			}
			break;
		case 2: // Range
			damage = RandomUtility.getRandom(250);
			npc.setNextAnimation(new Animation(13155));
			World.sendProjectile(npc, target, 2707, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getRangeHit(npc, damage));
			break;
		case 3: // Ice arrows range
			damage = RandomUtility.getRandom(250);
			npc.setNextAnimation(new Animation(13155));
			World.sendProjectile(npc, target, 369, 28, 16, 35, 35, 16, 0);
			delayHit(npc, 1, target, getRangeHit(npc, damage));
			break;
		case 4: // Orb crap
			break;
		}
		return defs.getAttackDelay();
	}
}