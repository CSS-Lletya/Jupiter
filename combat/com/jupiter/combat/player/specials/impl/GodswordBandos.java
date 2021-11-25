package com.jupiter.combat.player.specials.impl;

import java.util.Optional;

import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.combat.player.specials.WeaponSpecialSignature;
import com.jupiter.combat.player.specials.WeaponSpecials;
import com.jupiter.game.Entity;
import com.jupiter.game.item.ItemNames;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.encoders.other.Graphics;
import com.jupiter.skills.Skills;


@WeaponSpecialSignature(weapons = { ItemNames.BANDOS_GODSWORD }, specAmount = 100)
public class GodswordBandos implements WeaponSpecials {

	/**
	 *Attack does 15% more damage and drains one of the target's combat stats by 10% of damage dealt, rounded down, until it reaches 0. If the stat drained reaches 0
	 * before all of the damage could be accounted for, another stat is drained by the amount remaining. Stats are drained in the following order: Defence, Strength,
	 * Prayer, Attack, Magic, Ranged.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound");
		if (target instanceof Player) {
			;
		}
		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		int damage = combat.getRandomMaxHit(player, weaponId, attackStyle,
				false, true, 1.2, true);
		combat.delayNormalHit(weaponId, attackStyle,
				combat.getMeleeHit(player, damage));
		if (target instanceof Player) {
			Player targetPlayer = ((Player) target);
			int amountLeft;
			if ((amountLeft = targetPlayer.getSkills().drainLevel(
					Skills.DEFENCE, damage / 10)) > 0) {
				if ((amountLeft = targetPlayer.getSkills().drainLevel(
						Skills.STRENGTH, amountLeft)) > 0) {
					if ((amountLeft = targetPlayer.getSkills()
							.drainLevel(Skills.PRAYER, amountLeft)) > 0) {
						if ((amountLeft = targetPlayer.getSkills()
								.drainLevel(Skills.ATTACK, amountLeft)) > 0) {
							if ((amountLeft = targetPlayer.getSkills()
									.drainLevel(Skills.MAGIC,
											amountLeft)) > 0) {
								if (targetPlayer.getSkills()
										.drainLevel(Skills.RANGE,
												amountLeft) > 0) {
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(11991));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.of(new Graphics(2114));
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.empty();
	}
}