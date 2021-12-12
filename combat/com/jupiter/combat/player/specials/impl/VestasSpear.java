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


@WeaponSpecialSignature(weapons = { ItemNames.VESTAS_SPEAR }, specAmount = 50)
public class VestasSpear implements WeaponSpecials {

	/**
	 *Damages everyone adjacent and prevents melee attacks from striking you for 5 seconds.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound and implementation!");
		if (target instanceof Player) {
			;
		}
		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		combat.delayNormalHit(weaponId,	attackStyle, combat.getMeleeHit(player,
						combat.getRandomMaxHit(player, weaponId, attackStyle,
								false, true, 1.1, true)));
	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(10499));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.of(new Graphics(1835));
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.empty();
	}
}