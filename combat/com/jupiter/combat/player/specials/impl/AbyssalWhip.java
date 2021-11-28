package com.jupiter.combat.player.specials.impl;

import java.util.Optional;

import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.combat.player.specials.WeaponSpecialSignature;
import com.jupiter.combat.player.specials.WeaponSpecials;
import com.jupiter.game.Entity;
import com.jupiter.game.item.ItemNames;
import com.jupiter.game.player.Player;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.encoders.other.Graphics;

@WeaponSpecialSignature(weapons = { ItemNames.ABYSSAL_WHIP, 15442, 15443, 15444, 15441, 23691 }, specAmount = 50)
public class AbyssalWhip implements WeaponSpecials {

	/**
	 *An attack that transfers 25% of the target's run energy to the player (only works on other players).
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		target.setNextGraphics(new Graphics(2108, 0, 100));

		player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, needs sound, testing against player!");

		if (target instanceof Player) {
			Player targetPlayer = (Player) target;
			int runEnergyLoss = (int)Math.ceil(targetPlayer.getPlayerDetails().getRunEnergy()*0.25);
			targetPlayer.getMovement().setRunEnergy(targetPlayer.getPlayerDetails().getRunEnergy() - runEnergyLoss);
			player.getMovement().setRunEnergy(targetPlayer.getPlayerDetails().getRunEnergy() + runEnergyLoss);
		}
		int weaponId = player.getEquipment().getWeaponId();
		int attackStyle = player.getCombatDefinitions().getAttackStyle();
		int damage = combat.getRandomMaxHit(player, weaponId, attackStyle, false, true, 1, true);
		combat.delayNormalHit(weaponId, attackStyle, combat.getMeleeHit(player, damage));
	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(11971));
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