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


@WeaponSpecialSignature(weapons = { ItemNames.MAGIC_COMPOSITE_BOW }, specAmount = 35)
public class MagicCompositeBow implements WeaponSpecials {

	/**
	 *An accurate shot that has an increased chance of hitting your opponent.
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
		//combat.delayNormalHit(weaponId, attackStyle, combat.getMeleeHit(player));
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