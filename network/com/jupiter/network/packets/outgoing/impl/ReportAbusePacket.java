package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 100, description = "Represents a Report abuse packets")
public class ReportAbusePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted())
			return;
		int byte0 = stream.readUnsignedByte(); //correlating to first letter? keystroke > alphabet/numeric letter??
		String remaining_username = stream.readString(); //username missing the first letter, i.e when reporting "jordan" this will return "ordan"
		int byte2 = stream.readUnsignedByte(); //mute type i.e "bug abuse", "scamming"
		int byte3 = stream.readUnsignedByte(); // this is 1 when mute and 0 when no mute
		String string2 = stream.readString(); //doesn't return anything... empty.. ""
		
		String supposed_username = Utility.getCharacterFromByte(byte0) + remaining_username;
		
		System.out.println(String.format("b1: %s, string1: %s, b2: %s, b3: %s, string2: %s", byte0, remaining_username, byte2, byte3, string2));
		player.getPackets().sendGameMessage(supposed_username+"  "+ String.format("b1: %s, string1: %s, b2: %s, b3: %s, string2: %s", byte0, remaining_username, byte2, byte3, string2));
		/*@SuppressWarnings("unused")
		String username = stream.readString();
		@SuppressWarnings("unused")
		int type = stream.readUnsignedByte();
		@SuppressWarnings("unused")
		boolean mute = stream.readUnsignedByte() == 1;
		@SuppressWarnings("unused")
		String unknown2 = stream.readString();*/
	}
}