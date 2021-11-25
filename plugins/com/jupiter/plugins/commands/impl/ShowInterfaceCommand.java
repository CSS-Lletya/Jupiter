package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

@CommandSignature(alias = {"int"}, rights = {Rights.ADMINISTRATOR}, syntax = "Displays an interface")
public final class ShowInterfaceCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		if (cmd.length < 2) {
			player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
			return;
		}
		try {
			player.getInterfaceManager().sendInterface(Integer.valueOf(cmd[1]));
		} catch (NumberFormatException e) {
			player.getPackets().sendPanelBoxMessage("Use: ::inter interfaceId");
		}
	}
}