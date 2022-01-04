package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.actions.PlayerFollow;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.utility.Utility;

@LogicPacketSignature(packetId = 6, packetSize = 3, description = "The Second menu option for a Player")
public class PlayerOptionTwoPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
			return;
		@SuppressWarnings("unused")
		boolean unknown = stream.readByte() == 1;
		int playerIndex = stream.readUnsignedShortLE128();
		Player p2 = World.getPlayers().get(playerIndex);
		if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
			return;
		if (player.getMovement().getLockDelay() > Utility.currentTimeMillis())
			return;
		player.getAttributes().stopAll(player, false);
		player.getActionManager().setAction(new PlayerFollow(p2));
	}
}