package com.jupiter.combat.player.specials;

import java.util.Optional;

import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.game.Entity;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;

/**
 * Represents a Weapon with a Special attack event
 * @author Dennis
 *
 */
public interface WeaponSpecials {
	
	/**
	 * Executes the weapons special attack
	 * @param player
	 * @param target
	 * @param combat
	 * @throws Exception
	 */
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception;
	
	/**
	 * If the Animation is present, perform the specified Animation
	 * @return animation
	 */
	public Optional<Animation> getAnimation();
	
	/**
	 * If the Graphics is present, perform the specified Graphics
	 * @return animation
	 */
	public Optional<Graphics> getGraphics();
	
	/**
	 * If the Sound is present, play the sound
	 * @return sound
	 */
	public Optional<Integer> getSound();
}