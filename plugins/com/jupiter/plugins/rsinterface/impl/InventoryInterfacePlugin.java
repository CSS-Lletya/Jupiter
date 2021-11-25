package com.jupiter.plugins.rsinterface.impl;

import java.util.List;

import com.jupiter.Settings;
import com.jupiter.cache.io.InputStream;
import com.jupiter.combat.npc.NPC;
import com.jupiter.cores.WorldThread;
import com.jupiter.game.item.FloorItem;
import com.jupiter.game.item.Item;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Inventory;
import com.jupiter.game.player.Player;
import com.jupiter.game.route.CoordsEvent;
import com.jupiter.game.task.Task;
import com.jupiter.net.decoders.WorldPacketsDecoder;
import com.jupiter.plugins.InventoryDispatcher;
import com.jupiter.plugins.listener.RSInterface;
import com.jupiter.plugins.wrapper.RSInterfaceSignature;
import com.jupiter.skills.cooking.Foods;
import com.jupiter.utils.Logger;
import com.jupiter.utils.Utils;

@RSInterfaceSignature(interfaceId = {679})
public class InventoryInterfacePlugin implements RSInterface {

	@Override
	public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
		if (componentId == 0) {
			if (slotId > 27 || player.getInterfaceManager().containsInventoryInter())
				return;
			Item item = player.getInventory().getItem(slotId);
			if (item == null || item.getId() != slotId2)
				return;
			
			switch(packetId) {
			case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
				long time = Utils.currentTimeMillis();
				if (player.getLockDelay() >= time || player.getNextEmoteEnd() >= time)
					return;
				player.stopAll(false);
				if (Foods.eat(player, item, slotId))
					return;
				InventoryDispatcher.execute(player, item, 1);
				break;
			case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
				if (player.isEquipDisabled())
					return;
				long passedTime = Utils.currentTimeMillis() - WorldThread.WORLD_CYCLE;
				World.get().submit(new Task(passedTime >= 600 ? 0 : passedTime > 330 ? 1 : 0) {
					
					@Override
					protected void execute() {
						List<Byte> slots = player.getSwitchItemCache();
						int[] slot = new int[slots.size()];
						for (int i = 0; i < slot.length; i++)
							slot[i] = slots.get(i);
						player.getSwitchItemCache().clear();
						InventoryInterfaceTypePlugin.sendWear(player, slot);
						player.stopAll(false, true, false);
						this.cancel();
					}
				});
				if (player.getSwitchItemCache().contains(slotId))
					return;
				player.getSwitchItemCache().add(slotId);
				InventoryDispatcher.execute(player, item, 2);
				break;
			case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
				InventoryDispatcher.execute(player, item, 3);
				break;
			case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
				InventoryDispatcher.execute(player, item, 4);
				break;
			case WorldPacketsDecoder.ACTION_BUTTON5_PACKET:
				InventoryDispatcher.execute(player, item, 5);
				break;
			case WorldPacketsDecoder.ACTION_BUTTON6_PACKET:
				InventoryDispatcher.execute(player, item, 6);
				break;
			case WorldPacketsDecoder.ACTION_BUTTON8_PACKET:
				long dropTime = Utils.currentTimeMillis();
				if (player.getLockDelay() >= dropTime || player.getNextEmoteEnd() >= dropTime)
					return;
				if (!player.getControlerManager().canDropItem(item))
					return;
				player.stopAll(false);
				
				if (item.getDefinitions().isOverSized()) {
					player.getPackets().sendGameMessage("The item appears to be oversized.");
					player.getInventory().deleteItem(item);
					return;
				}

				if(player.getToolbelt().getToolbeltItems().contains(item.getId())) {
					player.getToolbelt().addItem(slotId, item);
					return;
				}
				InventoryDispatcher.execute(player, item, 7);
				player.getInventory().deleteItem(slotId, item);
				FloorItem.createGroundItem(item, new WorldTile(player), player, false, 180, true);
				player.getPackets().sendSound(2739, 0, 1);
				break;
			case 81:
				player.getInventory().sendExamine(slotId);
				InventoryDispatcher.execute(player, item, 8);
				break;
			}
		}
	}
	
	public static void handleItemOnItem(final Player player, InputStream stream) {
		int toSlot = stream.readShortLE128();
		int fromSlot = stream.readShortLE();
		int itemUsedWithId = stream.readShortLE128();
		int interfaceId2 = stream.readIntLE() >> 16;
		int interfaceId = stream.readIntV2() >> 16;
		int itemUsedId = stream.readShortLE();
		
		if (Settings.DEBUG)
			System.out.println(String.format("fromInter: %s, toInter: %s, fromSlot: %s, toSlot %s, item1: %s, item2: %s", interfaceId, interfaceId2, fromSlot, toSlot, itemUsedId, itemUsedWithId));
		
		//fromInter: 44498944, toInter: 44498944, fromSlot: 11694, toSlot 0, item1: 14484, item2: 8

		if (interfaceId == Inventory.INVENTORY_INTERFACE && interfaceId == interfaceId2
				&& !player.getInterfaceManager().containsInventoryInter()) {
			if (toSlot >= 28 || fromSlot >= 28)
				return;
			Item usedWith = player.getInventory().getItem(toSlot);
			Item itemUsed = player.getInventory().getItem(fromSlot);
			if (itemUsed == null || usedWith == null || itemUsed.getId() != itemUsedId
					|| usedWith.getId() != itemUsedWithId)
				return;
			player.stopAll();
			
			if (Settings.DEBUG)
				Logger.log("ItemHandler", "Used:" + itemUsed.getId() + ", With:" + usedWith.getId());
		}
	}

	public static void handleItemOnNPC(final Player player, final NPC npc, final Item item) {
		if (item == null) {
			return;
		}
		player.setCoordsEvent(new CoordsEvent(npc, new Runnable() {
			@Override
			public void run() {
				if (!player.getInventory().containsItem(item.getId(), item.getAmount())) {
					return;
				}
			}
		}, npc.getSize()));
	}
}