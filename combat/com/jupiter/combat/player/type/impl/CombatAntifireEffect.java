package com.jupiter.combat.player.type.impl;

import java.util.Optional;

import com.jupiter.combat.player.type.AntifireDetails;
import com.jupiter.combat.player.type.CombatEffect;
import com.jupiter.combat.player.type.AntifireDetails.AntifireType;
import com.jupiter.game.Entity;
import com.jupiter.game.player.Player;

/**
 * The class which is responsible for the effect when you drink an anti-fire potion.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class CombatAntifireEffect extends CombatEffect {

	/**
	 * The type of antifire this player has drunk.
	 */
	private final AntifireType type;

	/**
	 * Constructs a new {@link CombatAntifireEffect}.
	 * @param type {@link #type}.
	 */
	public CombatAntifireEffect(AntifireType type) {
		super(1);
		this.type = type;
	}

	@Override
	public boolean apply(Entity entity) {
		if(!(entity instanceof Player)) {
			return false;
		}
		Player player = (Player) entity;
		if(player.getAntifireDetails().isPresent()) {
			player.setAntifireDetails(Optional.of(new AntifireDetails(type)));
			return false;
		}
		player.setAntifireDetails(Optional.of(new AntifireDetails(type)));
		return true;
	}

	@Override
	public boolean removeOn(Entity entity) {
		if(entity instanceof Player) {
			Player player = (Player) entity;
			return !player.getAntifireDetails().isPresent() ? true : false;
		}
		return true;
	}

	@Override
	public void process(Entity entity) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (player.getAntifireDetails().isPresent()) {
				AntifireDetails detail = player.getAntifireDetails().get();
				int count = detail.getAntifireDelay().decrementAndGet();
				if (count == 30) {
					player.getPackets().sendGameMessage("Your resistance to dragon fire is about to wear off!");
				}
				if (count < 1) {
					player.setAntifireDetails(Optional.empty());
					player.getPackets().sendGameMessage("Your resistance to dragon fire has worn off!");
				}
			}
		}
	}

	@Override
	public boolean onLogin(Entity entity) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			return player.getAntifireDetails().isPresent() ? true : false;
		}
		return false;
	}
}