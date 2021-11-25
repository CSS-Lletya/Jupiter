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


@WeaponSpecialSignature(weapons = { ItemNames.DARK_BOW }, specAmount = 25)
public class DarkBow implements WeaponSpecials {

	/**
	 *Fires two arrows and inflicts up to 30% more damage than a normal shot. Minimum damage is 100 (50 damage per arrow). Recommended is using rune arrows with this special attack unless you've got dragon arrows .
	 * Descent of Dragons. 65% drain.
	 * When using dragon arrows, inflicts up to 50% more damage. Minimum damage is 80 per arrow or 48 per arrow with Protect from Missiles.
	 * Both attacks will always hit. Unless Disruption Shield was used prior to the Special Attack.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		target.setNextGraphics(new Graphics(2108, 0, 100));
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound, graphics, animations and implementation!");

		if (target instanceof Player) {
			;
		}
		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		int damage = 0;//getRandomMaxHit(player, weaponId, attackStyle, )
		//combat.delayNormalHit(weaponId, attackStyle, combat.getMeleeHit(player));int weaponId = player.getEquipment().getWeaponId();
	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.empty();
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.empty();
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.empty();
	}
}