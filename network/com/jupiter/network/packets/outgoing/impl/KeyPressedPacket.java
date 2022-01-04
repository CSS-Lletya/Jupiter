package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = 28, description = "Represents a key pressed event")
public class KeyPressedPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		int short0 = stream.readUnsignedShort();
		// Can utilize this packet to Close interfaces, open URLS based on key press, such.
//		player.getPackets().sendGameMessage("pressed: "+Utils.getKeyPressedFromListenerByte(short0));
		switch (short0) {
			case 3328:
				player.getInterfaceManager().closeInterfaces();
		}
	}
}