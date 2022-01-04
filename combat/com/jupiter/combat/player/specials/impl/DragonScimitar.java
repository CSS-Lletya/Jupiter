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
import com.jupiter.network.encoders.other.Hit;


@WeaponSpecialSignature(weapons = { ItemNames.DRAGON_SCIMITAR }, specAmount = 55)
public class DragonScimitar implements WeaponSpecials {

	/**
	 * A slash with increased accuracy that, if successful,
	 * prevents the target from using protection prayers for five seconds.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, needs accuracy implementation and testing!");
		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		Hit hit11 = combat.getMeleeHit(
				player,
				combat.getRandomMaxHit(player, weaponId, attackStyle, false,
						true, 1.0, true));
		if (target instanceof Player) {
			Player p2 = (Player) target;
			if (hit11.getDamage() > 0)
				p2.setPrayerDelay(5000);// 5 seconds
		}
		combat.delayNormalHit(weaponId, attackStyle, hit11);
	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(12031));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.of(new Graphics(2118));
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.of(2540);
	}
}