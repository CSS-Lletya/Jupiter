package com.jupiter.game;

import com.jupiter.combat.npc.NPC;
import com.jupiter.game.player.Player;

/**
 * The enumerated type whose elements represent the different types of {@link Entity} implementations.
 * @author lare96 <http://github.com/lare96>
 */
public enum EntityType {
	
	/**
	 * The element used to represent the {@link Player} implementation.
	 */
	PLAYER,
	
	/**
	 * The element used to represent the {@link NPC} implementation.
	 */
	NPC,
}