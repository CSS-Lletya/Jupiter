package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.cache.utility.CacheUtility;
import com.jupiter.game.item.Item;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Inventory;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.plugin.PluginManager;
import com.jupiter.plugin.events.ItemOnObjectEvent;
import com.jupiter.utility.Utility;

@LogicPacketSignature(packetId = 98, packetSize = 17, description = "An Interface that's used onto a Object (Magic, etc..)")
public class InterfaceOnObjectPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		int x = stream.readUnsignedShortLE128();
	 	boolean forceRun = stream.readUnsigned128Byte() == 1;
	 	int objectId = stream.readIntV1();
	 	int interfaceHash = stream.readInt();
	 	int itemId = stream.readUnsignedShortLE();
	 	int slot = stream.readUnsignedShort128();
	 	int y = stream.readUnsignedShortLE();
		
	 	int interfaceId = interfaceHash >> 16;
	 	int componentId = interfaceHash - (interfaceId << 16);
		
		System.out.println(String.format("%s, %s, %s, %s, %s, %s, %s", x, forceRun, objectId, interfaceHash, itemId, slot, y));
		System.out.println(String.format("%s, %s,", interfaceId, componentId));
		
		//3095, 0, 17010, 44498944, 11694, 0, 3503
//		0, 17010,
		
		if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
			return;
		long currentTime = Utility.currentTimeMillis();
		if (player.getMovement().getLockDelay() >= currentTime || player.getNextEmoteEnd() >= currentTime)
			return;
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId))
			return;
		WorldObject mapObject = World.getRegion(regionId).getObject(objectId, tile);
		if (mapObject == null || mapObject.getId() != objectId)
			return;
		final WorldObject object = !player.isAtDynamicRegion() ? mapObject : new WorldObject(objectId, mapObject.getType(), mapObject.getRotation(), x, y, player.getPlane());
		final Item item = player.getInventory().getItem(slot);
		if (player.isDead() || CacheUtility.getInterfaceDefinitionsSize() <= interfaceId)
			return;
		if (player.getMovement().getLockDelay() > Utility.currentTimeMillis())
			return;
		if (!player.getInterfaceManager().containsInterface(interfaceId))
			return;
		if (item == null || item.getId() != itemId)
			return;
		player.getAttributes().stopAll(player, false); // false
		if (forceRun)
			player.setRun(forceRun);
		switch (interfaceId) {
		case Inventory.INVENTORY_INTERFACE: // inventory
			PluginManager.handle(new ItemOnObjectEvent(player, item, object, false));
			break;
		}
	}
}