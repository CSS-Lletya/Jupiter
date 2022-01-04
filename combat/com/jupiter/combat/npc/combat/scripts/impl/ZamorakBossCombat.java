package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.combat.player.type.PoisonType;
import com.jupiter.game.Entity;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.ForceTalk;
import com.jupiter.network.encoders.other.Graphics;
import com.jupiter.skills.prayer.Prayer;
import com.jupiter.utility.RandomUtility;

@MobCombatSignature(mobId = {6203}, mobName = {})
public class ZamorakBossCombat extends MobCombatInterface {

	@Override
	public int execute(Player target, NPC npc) throws Exception {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (RandomUtility.random(4) == 0) {
			switch (RandomUtility.random(8)) {
			case 0:
				npc.setNextForceTalk(new ForceTalk("Attack them, you dogs!"));
				break;
			case 1:
				npc.setNextForceTalk(new ForceTalk("Forward!"));
				break;
			case 2:
				npc.setNextForceTalk(new ForceTalk("Death to Saradomin's dogs!"));
				break;
			case 3:
				npc.setNextForceTalk(new ForceTalk("Kill them, you cowards!"));
				break;
			case 4:
				npc.setNextForceTalk(new ForceTalk("The Dark One will have their souls!"));
				npc.playSound(3229, 2);
				break;
			case 5:
				npc.setNextForceTalk(new ForceTalk("Zamorak curse them!"));
				break;
			case 6:
				npc.setNextForceTalk(new ForceTalk("Rend them limb from limb!"));
				break;
			case 7:
				npc.setNextForceTalk(new ForceTalk("No retreat!"));
				break;
			case 8:
				npc.setNextForceTalk(new ForceTalk("Flay them all!"));
				break;
			}
		}
		int attackStyle = RandomUtility.random(2);
		switch (attackStyle) {
		case 0:// magic flame attack
			npc.setNextAnimation(new Animation(14962));
			npc.setNextGraphics(new Graphics(1210));
			for (Entity t : npc.getPossibleTargets()) {
				delayHit(npc, 1, t, getMagicHit(npc, getRandomMaxHit(npc, 300, NPCCombatDefinitions.MAGE, t)));
				World.sendProjectile(npc, t, 1211, 41, 16, 41, 35, 16, 0);
				if (RandomUtility.random(4) == 0)
					t.poison(PoisonType.SUPER_MELEE);
			}
			break;
		case 1:// main attack
		case 2:// melee attack
			int damage = 463;// normal
			for (Entity e : npc.getPossibleTargets()) {
				if (e instanceof Player && (((Player) e).getPrayer().active(Prayer.PROTECT_MELEE)
						|| ((Player) e).getPrayer().active(Prayer.DEFLECT_MELEE))) {
					Player player = (Player) e;
					damage = 497;
					npc.setNextForceTalk(new ForceTalk("YARRRRRRR!"));
					player.getPrayer().drainPrayer((Math.round(damage / 20)));
					player.setPrayerDelay(RandomUtility.random(5) + 5);
					player.getPackets().sendGameMessage(
							"K'ril Tsutsaroth slams through your protection prayer, leaving you feeling drained.");
				}
				npc.setNextAnimation(new Animation(damage <= 463 ? 14963 : 14968));
				delayHit(npc, 0, e, getMeleeHit(npc, getRandomMaxHit(npc, damage, NPCCombatDefinitions.MELEE, e)));
			}
			break;
		}
		return defs.getAttackDelay();
	}
}