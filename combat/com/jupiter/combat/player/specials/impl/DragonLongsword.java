package com.jupiter.combat.player.specials.impl;

import java.util.Optional;

import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.combat.player.specials.WeaponSpecialSignature;
import com.jupiter.combat.player.specials.WeaponSpecials;
import com.jupiter.game.Animation;
import com.jupiter.game.Entity;
import com.jupiter.game.Graphics;
import com.jupiter.game.item.ItemNames;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;


@WeaponSpecialSignature(weapons = { ItemNames.DRAGON_LONGSWORD }, specAmount = 25)
public class DragonLongsword implements WeaponSpecials {

	/**
	 *Deals extra damage.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		if(player.getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound, ans testing!");
		if (target instanceof Player) {
			;
		}
		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		int damage = 0;//getRandomMaxHit(player, weaponId, attackStyle, )
		combat.delayNormalHit(
				weaponId,
				attackStyle,
				combat.getMeleeHit(
						player,
						combat.getRandomMaxHit(player, weaponId, attackStyle,
								false, true, 1.25, true)));

	}
	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(12033));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.of(new Graphics(2117));
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.empty();
	}
}