package com.jupiter.plugins.commands.impl;

import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

@CommandSignature(alias = {"tele"}, rights = {Rights.ADMINISTRATOR}, syntax = "Teleports you to a specified location")
public final class TeleportCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		if (cmd.length < 3) {
			player.getPackets().sendPanelBoxMessage("Use: ::tele coordX coordY");
			return;
		}
		try {
			player.resetWalkSteps();
			player.setNextWorldTile(new WorldTile(Integer.valueOf(cmd[1]), Integer.valueOf(cmd[2]),
					cmd.length >= 4 ? Integer.valueOf(cmd[3]) : player.getPlane()));
		} catch (NumberFormatException e) {
			player.getPackets().sendPanelBoxMessage("Use: ::tele coordX coordY plane");
		}
	}
}