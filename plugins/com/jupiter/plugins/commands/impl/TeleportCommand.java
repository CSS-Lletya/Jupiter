package com.jupiter.plugins.commands.impl;

import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

@CommandSignature(alias = {"tele"}, rights = {Rights.ADMINISTRATOR}, syntax = "Teleports you to a specified location")
public final class TeleportCommand implements Command {
	
	@Override
	public void execute(Player p, String[] args, String command) throws Exception {
		if (args.length < 3) {
			p.getPackets().sendPanelBoxMessage("Use: ::tele coordX coordY");
			return;
		}
		if (args[0].contains(",")) {
			args = args[0].split(",");
			int plane = Integer.valueOf(args[0]);
			int x = Integer.valueOf(args[1]) << 6 | Integer.valueOf(args[3]);
			int y = Integer.valueOf(args[2]) << 6 | Integer.valueOf(args[4]);
			p.resetWalkSteps();
			p.setNextWorldTile(new WorldTile(x, y, plane));
		} else if (args.length == 1) {
			p.resetWalkSteps();
			p.setNextWorldTile(new WorldTile(Integer.valueOf(args[0])));
		} else {
			p.resetWalkSteps();
			p.setNextWorldTile(new WorldTile(Integer.valueOf(args[0]), Integer.valueOf(args[1]), args.length >= 3 ? Integer.valueOf(args[2]) : p.getPlane()));
		}
	}
}