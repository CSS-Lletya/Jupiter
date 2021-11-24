package com.jupiter.plugins.commands.impl;

import com.jupiter.game.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;
import com.rs.net.host.HostListType;
import com.rs.net.host.HostManager;

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