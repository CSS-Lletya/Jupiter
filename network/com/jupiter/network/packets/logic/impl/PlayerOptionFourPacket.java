package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.utility.Utility;

@LogicPacketSignature(packetId = 89, packetSize = 3, description = "The Fourth menu option for a Player")
public class PlayerOptionFourPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		@SuppressWarnings("unused")
		boolean unknown = stream.readByte() == 1;
		int playerIndex = stream.readUnsignedShort(); //incorrect returns 32k
		Player p2 = World.getPlayers().get(playerIndex);
		if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
			return;
		if (player.getMovement().getLockDelay() > Utility.currentTimeMillis())
			return;
		player.getAttributes().stopAll(player, false);
		if (player.isCantTrade()) {
			player.getPackets().sendGameMessage("You are busy.");
			return;
		}
		if (p2.getInterfaceManager().containsScreenInter() || p2.isCantTrade()) {
			player.getPackets().sendGameMessage("The other player is busy.");
			return;
		}
		if (!p2.withinDistance(player, 14)) {
			player.getPackets().sendGameMessage("Unable to find target " + p2.getDisplayName());
			return;
		}

		if (p2.getTemporaryAttributtes().get("TradeTarget") == player) {
			p2.getTemporaryAttributtes().remove("TradeTarget");
			player.getTrade().openTrade(p2);
			p2.getTrade().openTrade(player);
			return;
		}
		player.getTemporaryAttributtes().put("TradeTarget", p2);
		player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() + " a request...");
		p2.getPackets().sendTradeRequestMessage(player);
	}
}