package com.jupiter.plugins.listener;

import com.jupiter.combat.npc.NPC;
import com.jupiter.game.player.Player;

/**
 * 
 * @author Dennis
 *
 */
public interface NPCType {
	
	/**
	 * The functionality to be executed as soon as this execution is called.
	 * @param player the player we are executing this command for.
	 * @param cmd the command that we are executing for this player.
	 */
	void execute(Player player, NPC npc, int option) throws Exception;
}
