package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.net.encoders.other.Graphics;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

@CommandSignature(alias = {"gfx", "graphic", "graphics"}, rights = {Rights.ADMINISTRATOR}, syntax = "Perform an graphic")
public final class GraphicsCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		player.setNextGraphics(new Graphics(Integer.valueOf(cmd[1])));
	}
}