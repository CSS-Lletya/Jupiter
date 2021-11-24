package com.jupiter.plugins.commands.impl;

import com.jupiter.game.Hit;
import com.jupiter.game.World;
import com.jupiter.game.Hit.HitLook;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

@CommandSignature(alias = {"kill"}, rights = {Rights.ADMINISTRATOR}, syntax = "Kill a specified Player")
public final class KillCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		String name;
		Player target;
		name = "";
		for (int i = 1; i < cmd.length; i++)
			name += cmd[i] + ((i == cmd.length - 1) ? "" : " ");
		target = World.getPlayerByDisplayName(name);
		if (target == null)
			return;
		target.applyHit(new Hit(target, player.getHitpoints(), HitLook.REGULAR_DAMAGE));
		target.stopAll();
	}
}