package com.jupiter.network.packets.outgoing.impl;

import java.util.List;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.encoders.other.PublicChatMessage;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.network.utility.Huffman;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.LogUtility.Type;
import com.jupiter.utility.Utility;

@OutgoingPacketSignature(packetId = 86, description = "Represents a Chat message event")
public class ChatPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted())
			return;
		if (player.getLastPublicMessage() > Utility.currentTimeMillis())
			return;
		player.setLastPublicMessage(Utility.currentTimeMillis() + 300);
		int colorEffect = stream.readUnsignedByte();
		int moveEffect = stream.readUnsignedByte();
		String message = Huffman.readEncryptedMessage(200, stream);
		if (message == null || message.replaceAll(" ", "").equals(""))
			return;
		if (message.startsWith("::") || message.startsWith(";;")) {
			// if command exists and processed wont send message as public
			// message
			CommandsPacket.processCommand(player, message.replace("::", "").replace(";;", ""), false, false);
			return;
		}
		if (player.getPlayerDetails().getMuted() > Utility.currentTimeMillis()) {
			player.getPackets().sendGameMessage("You temporary muted. Recheck in 48 hours.");
			return;
		}
		int effects = (colorEffect << 8) | (moveEffect & 0xff);
		if (player.getPlayerDetails().getChatType() == 1)
			player.getCurrentFriendChat().sendFriendsChannelMessage(player, Utility.fixChatMessage(message));
		else
			sendPublicChatMessage(player, new PublicChatMessage(Utility.fixChatMessage(message), effects));
		 
			LogUtility.log(Type.INFO, "World Packet Decoder", "Chat type: " + player.getPlayerDetails().getChatType());
	}
	
	public static void sendPublicChatMessage(Player player, PublicChatMessage message) {
		for (int regionId : player.getMapRegionsIds()) {
			List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player p = World.getPlayers().get(playerIndex);
				if (p == null || !p.isStarted() || p.hasFinished()
						|| p.getLocalPlayerUpdate().getLocalPlayers()[player.getIndex()] == null)
					continue;
				p.getPackets().sendPublicMessage(player, message);
			}
		}
	}
}