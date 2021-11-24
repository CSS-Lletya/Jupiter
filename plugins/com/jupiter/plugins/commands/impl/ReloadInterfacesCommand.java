package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.RSInterfaceDispatcher;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

@CommandSignature(alias = {"reloadint"}, rights = {Rights.ADMINISTRATOR}, syntax = "Reloads the RS Interface system")
public final class ReloadInterfacesCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		RSInterfaceDispatcher.reload();
	}
}