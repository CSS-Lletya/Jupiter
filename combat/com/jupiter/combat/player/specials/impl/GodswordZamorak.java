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


@WeaponSpecialSignature(weapons = { ItemNames.ZAMORAK_GODSWORD }, specAmount = 60)
public class GodswordZamorak implements WeaponSpecials {

	/**
	 *Freezes the target for 20 seconds if successful, though the target can still attack a player in an adjacent square.
	 * Also increase damage by 10% for the attack
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound, testing and implementation!");

		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		int zgsdamage = combat.getRandomMaxHit(player, weaponId, attackStyle,
				false, true, 1.0, true);
		if (zgsdamage != 0 && target.getSize() <= 1) { // freezes small
			// npcs
			target.setNextGraphics(new Graphics(2104));
			target.addFreezeDelay(18000); // 18seconds
		}
		combat.delayNormalHit(weaponId, attackStyle,
				combat.getMeleeHit(player, zgsdamage));
	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(7070));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.of(new Graphics(1221));
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.empty();
	}
}