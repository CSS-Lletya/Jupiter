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


@WeaponSpecialSignature(weapons = { ItemNames.ARMADYL_GODSWORD }, specAmount = 50)
public class GodswordArmadyl implements WeaponSpecials {

	/**
	 *Inflicts 25% more damage.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound and testing!");

		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		combat.delayNormalHit(weaponId, attackStyle, combat.getMeleeHit(
						player,	combat.getRandomMaxHit(player, weaponId, attackStyle,
								false, true, 1.375, true)));
	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(11989));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.of(new Graphics(2113));
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.empty();
	}
}