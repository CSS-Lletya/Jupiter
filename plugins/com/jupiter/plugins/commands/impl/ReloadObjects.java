package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.ObjectDispatcher;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

@CommandSignature(alias = {"reloadobjects"}, rights = {Rights.ADMINISTRATOR}, syntax = "Reloads the Objects list")
public final class ReloadObjects implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		ObjectDispatcher.load();
	}
}