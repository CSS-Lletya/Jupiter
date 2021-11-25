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


@WeaponSpecialSignature(weapons = { ItemNames.STATIUS_WARHAMMER }, specAmount = 35)
public class StatiusWarhammer implements WeaponSpecials {

	/**
	 *Deals up to 25% more damage and decreases opponent's Defence by 30%.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound, needs -30% def implementation!");


		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
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
		return Optional.of(new Animation(10505));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.of(new Graphics(1040));
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.empty();
	}
}