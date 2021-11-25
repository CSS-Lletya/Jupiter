package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandDispatcher;
import com.jupiter.plugins.commands.CommandSignature;

@CommandSignature(alias = {"reloadcommands"}, rights = {Rights.ADMINISTRATOR}, syntax = "Reloads the Commands list")
public final class ReloadCommands implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		CommandDispatcher.load();
	}
}