package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.network.utility.Huffman;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 15, description = "Represents a friends chat message event")
public class SendFriendsChatMessagePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted())
			return;
		if (player.getPlayerDetails().getMuted() > Utility.currentTimeMillis()) {
			player.getPackets().sendGameMessage("You temporary muted. Recheck in 48 hours.");
			return;
		}
		String username = stream.readString();
		Player p2 = World.getPlayerByDisplayName(username);
		if (p2 == null)
			return;
		player.getFriendsIgnores().sendMessage(p2, Utility.fixChatMessage(Huffman.readEncryptedMessage(150, stream)));
	}
}