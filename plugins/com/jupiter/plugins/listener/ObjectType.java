package com.jupiter.plugins.listener;

import com.jupiter.game.WorldObject;
import com.jupiter.game.player.Player;

/**
 * 
 * @author Dennis
 *
 */
public interface ObjectType {
	
	/**
	 * The functionality to be executed as soon as this command is called.
	 * @param player the player we are executing this command for.
	 * @param cmd the command that we are executing for this player.
	 */
	void execute(Player player, WorldObject object, int optionId) throws Exception;
}
