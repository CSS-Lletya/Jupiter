package com.jupiter.plugins.commands.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

@CommandSignature(alias = { "npc" }, rights = { Rights.ADMINISTRATOR }, syntax = "Spawns a npc with the specified ID")
public final class NPCCommand implements Command {

	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		try {
			NPC.spawnNPC(Integer.parseInt(cmd[1]), player, true, true);
			return;
		} catch (NumberFormatException e) {
			player.getPackets().sendPanelBoxMessage("Use: ::npc id(Integer)");
		}
	}
}