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


@WeaponSpecialSignature(weapons = { ItemNames.DRAGON_DAGGER, ItemNames.DRAGON_DAGGER_P, ItemNames.DRAGON_DAGGER_P_PLUS, ItemNames.DRAGON_DAGGER_P_PLUS_PLUS},
		specAmount = 25)
public class DragonDagger implements WeaponSpecials {

	/**
	 *Two quick slashes with increased accuracy and damage. Often used to quickly finish off opponents by high-level players.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound, graphics, animations and implementation!");

		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		double ddsmult = 0;
		if ((player.getEquipment().getShieldId() >= 26579 && player.getEquipment().getShieldId() <= 26602) || (player.getEquipment().getShieldId() >= 25674 && player.getEquipment().getShieldId() <= 27072)) {
			ddsmult = 0.2;
		} else {
			ddsmult = 1.15;
		}
		combat.delayNormalHit(
				weaponId,
				attackStyle,
				combat.getMeleeHit(
						player,
						combat.getRandomMaxHit(player, weaponId, attackStyle,
								false, true, ddsmult, true)),
				combat.getMeleeHit(
						player,
						combat.getRandomMaxHit(player, weaponId, attackStyle,
								false, true, ddsmult, true)));

	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(1062));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.of(new Graphics(252, 0, 100));
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.of(2537);
	}
}