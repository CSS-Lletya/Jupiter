package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.plugins.rsinterface.impl.InventoryInterfacePlugin;

@OutgoingPacketSignature(packetId = 4, description = "Represents a Player's AFK state")
public class InterfaceOnInterfacePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		InventoryInterfacePlugin.handleItemOnItem(player, stream);
	}
}