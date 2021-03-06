package com.jupiter.combat.player.specials.impl;

import java.util.Optional;

import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.combat.player.specials.WeaponSpecialSignature;
import com.jupiter.combat.player.specials.WeaponSpecials;
import com.jupiter.game.Entity;
import com.jupiter.game.item.ItemNames;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;


@WeaponSpecialSignature(weapons = { ItemNames.SARADOMIN_GODSWORD }, specAmount = 50)
public class GodswordSaradomin implements WeaponSpecials {

	/**
	 *Attack deals 10% more damage and restores the user's life points by 50% of the damage dealt (with a minimum of 100 life points) and Prayer by 25% of
	 * damage dealt (with a minimum of 50 Prayer points). The attack has no effect if it misses completely but will always take effect if the attack hits and
	 * deals at least 1 damage.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound and testing!");
		if (target instanceof Player) {
			;
		}
		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		int sgsdamage = combat.getRandomMaxHit(player, weaponId, attackStyle,
				false, true, 1.1, true);
		player.heal(sgsdamage / 2);
		player.getPrayer().restorePrayer((sgsdamage / 4) * 10);
		combat.delayNormalHit(weaponId, attackStyle,
				combat.getMeleeHit(player, sgsdamage));
	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(7071));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.of(new Graphics(2109));
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.empty();
	}
}