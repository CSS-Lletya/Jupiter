package com.jupiter.plugins.commands.impl;

import com.jupiter.game.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

/**
 * This is just a dummy command to re-use
 * for whatever testing needed.
 * @author Dennis
 *
 */
@CommandSignature(alias = {"update", "shutdown"}, rights = {Rights.ADMINISTRATOR}, syntax = "Shut down the server in a time based length")
public final class ShutdownCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		World.get().safeShutdown(false, Integer.valueOf(cmd[1]));
		World.players().forEach(p -> p.getPackets().sendGameMessage("Shutting down"));
	}
}