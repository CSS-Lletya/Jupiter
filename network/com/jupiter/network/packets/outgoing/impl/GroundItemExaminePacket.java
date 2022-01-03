package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.item.FloorItem;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = 61, description = "Represents a Ground Item examine")
public class GroundItemExaminePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted() || !player.isClientLoadedMapRegion()
				|| player.isDead())
			return;
		final int id = stream.readShortLE128();
		boolean forceRun =  stream.readUnsignedByteC() == 1; 
		int y = stream.readUnsignedShort();
		int x = stream.readUnsignedShort128();
		if (forceRun)
			player.setRun(true);
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		final int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId))
			return;
		final FloorItem item = World.getRegion(regionId).getGroundItem(id,
				tile, player);
		if (item == null)
			return;
		player.getAttributes().stopAll(player, false);
		final FloorItem floorItem = World.getRegion(regionId)
				.getGroundItem(id, tile, player);
		if (floorItem == null)
			return;
		player.getPackets().sendGameMessage("examined floor item");
	}
}