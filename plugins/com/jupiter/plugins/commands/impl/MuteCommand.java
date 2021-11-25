package com.jupiter.plugins.commands.impl;

import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.net.host.HostListType;
import com.jupiter.net.host.HostManager;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;
import com.jupiter.utils.Utils;

@CommandSignature(alias = {"mute"}, rights = {Rights.ADMINISTRATOR}, syntax = "Mute a specified Player")
public final class MuteCommand implements Command {
	
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
		HostManager.add(target, HostListType.MUTED_IP, true);
		target.getPlayerDetails().setMuted(Utils.currentTimeMillis() + (player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR ? (48 * 60 * 60 * 1000) : (1 * 60 * 60 * 1000)));
	}
}