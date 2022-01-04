package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.item.FloorItem;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.route.RouteEvent;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.utility.Utility;

@LogicPacketSignature(packetId = 54, packetSize = 7, description = "Takes an Item from the Ground tile")
public class ItemTakePacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
			return;
		long currentTime = Utility.currentTimeMillis();
		if (player.getMovement().getLockDelay() > currentTime || player.getFreezeDelay() >= currentTime)
			return;

		final int id = stream.readShortLE128();
		boolean forceRun =  stream.readUnsignedByteC() == 1; 
		int y = stream.readUnsignedShort();
		int x = stream.readUnsignedShort128();
		
		System.out.println(x+", "+ y +", "+id +", "+forceRun);
		
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		final int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId)){
			return;
		}
		final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
		if (item == null)
			return;
		player.getAttributes().stopAll(player, false);
		if (forceRun)
			player.setRun(forceRun);
		player.setRouteEvent(new RouteEvent(item, new Runnable() {
			@Override
			public void run() {
				final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
				if (item == null)
					return;
				/*
				 * if (player.getRights() > 0 || player.isSupporter())
				 * player.getPackets().sendGameMessage("This item was dropped by [Username] "+item.getOwner().getUsername()+
				 * " [DiplayName] "+item.getOwner().getDisplayName());
				 */ player.setNextFaceWorldTile(tile);
				player.addWalkSteps(tile.getX(), tile.getY(), 1);
				FloorItem.removeGroundItem(player, item);
			}
		}, false));
	}
}