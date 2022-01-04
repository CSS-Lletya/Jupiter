package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.Settings;
import com.jupiter.cache.io.InputStream;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.plugins.commands.CommandDispatcher;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.LogUtility.Type;

@OutgoingPacketSignature(packetId = 85, description = "Represents a Command event")
public class CommandsPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isActive())
			return;
		boolean clientCommand = stream.readUnsignedByte() == 1;
		@SuppressWarnings("unused")
		boolean unknown = stream.readUnsignedByte() == 1;
		String command = stream.readString();
		if (!processCommand(player, command, true, clientCommand) && Settings.DEBUG)
			LogUtility.log(Type.INFO, "World Packet Decoder", "Command: " + command);
	}
	
	public static boolean processCommand(Player player, String command, boolean console, boolean clientCommand) {
		if (command.length() == 0)
			return false;
		String[] cmd = command.toLowerCase().split(" ");
		if (cmd.length == 0)
			return false;
		if (clientCommand) {
			switch (cmd[0]) {
			//NOTE: There's a bug where in World Map if you shift click it crashes client, idk why.
			case "tele":
				cmd = cmd[1].split(",");
				int plane = Integer.valueOf(cmd[0]);
				int x = Integer.valueOf(cmd[1]) << 6 | Integer.valueOf(cmd[3]);
				int y = Integer.valueOf(cmd[2]) << 6 | Integer.valueOf(cmd[4]);
				player.setNextWorldTile(new WorldTile(x, y, plane));
				return true;
			}
		}
		CommandDispatcher.execute(player, cmd, command);
		return false;
	}
}