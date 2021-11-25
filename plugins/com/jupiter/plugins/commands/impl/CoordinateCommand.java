package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

/**
 * This is just a dummy command to re-use
 * for whatever testing needed.
 * @author Dennis
 *
 */
@CommandSignature(alias = {"coord", "coords"}, rights = {Rights.ADMINISTRATOR}, syntax = "Get your current coordinate")
public final class CoordinateCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		player.getPackets().sendGameMessage("x: "+ player.getX() + " y: " + player.getY() + " h: " + player.getPlane());
	}
}