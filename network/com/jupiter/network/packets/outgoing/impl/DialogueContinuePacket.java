package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.cache.utility.CacheUtility;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.LogUtility.Type;

@OutgoingPacketSignature(packetId = 49, description = "Represents a Dialogue cont state")
public class DialogueContinuePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		int interfaceHash = stream.readIntV1();//stream.readInt();
		int junk = stream.readShortLE128();//stream.readShort128();
		int interfaceId = interfaceHash >> 16;
		int buttonId = (interfaceHash & 0xFF);
		System.out.println("interId: "+interfaceId+", buttonId: "+buttonId);

		if (CacheUtility.getInterfaceDefinitionsSize() <= interfaceId) {
			// hack, or server error or client error
			// player.getSession().getChannel().close();
			return;
		}
		if (!player.isActive() || !player.getInterfaceManager().containsInterface(interfaceId))
			return;
		
			LogUtility.log(Type.INFO, "World Packet Decoder", "Dialogue: " + interfaceId + ", " + buttonId + ", " + junk);
		int componentId = interfaceHash - (interfaceId << 16);
		player.getConversation().process(interfaceId, componentId);
	}
}