package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.Settings;
import com.jupiter.cache.io.InputStream;
import com.jupiter.cache.utility.CacheUtility;
import com.jupiter.game.player.Inventory;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;

@OutgoingPacketSignature(packetId = 74, description = "Represents a Item movement in an interface")
public class SwitchInterfaceItemPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		stream.readUnsignedShortLE();//skip in stream
		int fromSlot = stream.readUnsignedShortLE();
		int toSlot = stream.readUnsignedShortLE();
		stream.readUnsignedShortLE();//skip, idk what these are?
		int fromComponentId = stream.readShort();
		int fromInterfaceId = stream.readUnsignedShort();
		int toComponentId = stream.readUnsignedShortLE();

		//temporary, did I miss the from interface in the stream or is there no way to go between interfaces??
		int toInterfaceId = fromInterfaceId;

		if(Settings.DEBUG)
			System.out.println(String.format("fromInterfaceID: %s, toInterfaceID: %s, fromcompID: %s, tocompID %s, fromSlot: %s, toSlot: %s",
				fromInterfaceId, toInterfaceId, fromComponentId, toComponentId, fromSlot, toSlot));


		
		if (CacheUtility.getInterfaceDefinitionsSize() <= fromInterfaceId || CacheUtility.getInterfaceDefinitionsSize() <= toInterfaceId)
			return;
		if (!player.getInterfaceManager().containsInterface(fromInterfaceId) || !player.getInterfaceManager().containsInterface(toInterfaceId))
			return;
		if (fromComponentId != -1 && CacheUtility.getInterfaceDefinitionsComponentsSize(fromInterfaceId) <= fromComponentId)
			return;
		if (toComponentId != -1 && CacheUtility.getInterfaceDefinitionsComponentsSize(toInterfaceId) <= toComponentId)
			return;
		if (fromInterfaceId == Inventory.INVENTORY_INTERFACE && fromComponentId == 0 && toInterfaceId == Inventory.INVENTORY_INTERFACE && toComponentId == 0) {
			toSlot -= 28;
			if (toSlot < 0 || toSlot >= player.getInventory().getItemsContainerSize() || fromSlot >= player.getInventory().getItemsContainerSize())
				return;
			player.getInventory().switchItem(fromSlot, toSlot);
		} else if (fromInterfaceId == 763 && fromComponentId == 0 && toInterfaceId == 763 && toComponentId == 0) {
			if (toSlot >= player.getInventory().getItemsContainerSize() || fromSlot >= player.getInventory().getItemsContainerSize())
				return;
			player.getInventory().switchItem(fromSlot, toSlot);
		} else if (fromInterfaceId == 762 && toInterfaceId == 762) {
			player.getBank().switchItem(fromSlot, toSlot, fromComponentId, toComponentId);
		}
		
			System.out.println("Switch item interface " + fromInterfaceId + " from slot " + fromSlot + " to slot " + toSlot);
	}
}