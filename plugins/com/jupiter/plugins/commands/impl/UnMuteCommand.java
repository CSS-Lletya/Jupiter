package com.jupiter.plugins.commands.impl;

import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.net.host.HostListType;
import com.jupiter.net.host.HostManager;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

@CommandSignature(alias = {"unmute"}, rights = {Rights.ADMINISTRATOR}, syntax = "Un-mute a specified Player")
public final class UnMuteCommand implements Command {
	
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
		HostManager.remove(cmd[1], HostListType.MUTED_IP, true);
		target.getPlayerDetails().setMuted(0);
	}
}