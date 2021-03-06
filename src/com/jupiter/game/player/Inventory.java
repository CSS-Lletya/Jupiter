package com.jupiter.game.player;

import java.util.Arrays;
import java.util.function.Consumer;

import com.jupiter.cache.utility.CacheUtility;
import com.jupiter.game.item.Item;
import com.jupiter.game.item.ItemsContainer;
import com.jupiter.game.player.activity.ActivityHandler;
import com.jupiter.utility.ItemExamines;
import com.jupiter.utility.ItemWeights;

import io.vavr.collection.Array;
import lombok.Getter;

public final class Inventory {

	@Getter
	private ItemsContainer<Item> items;

	private transient Player player;

	public static final int INVENTORY_INTERFACE = 679;

	public Inventory() {
		items = new ItemsContainer<Item>(28, false);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void init() {
		player.getPackets().sendItems(93, items);
	}

	public void unlockInventoryOptions() {
		player.getPackets().sendAccessMask(INVENTORY_INTERFACE, 0, 0, 27, 12942734);
		player.getPackets().sendAccessMask(INVENTORY_INTERFACE, 0, 28, 55, 2097152);
	}

	public void reset() {
		items.reset();
		init();
	}

	public void refresh(byte... slots) {
		double w = 0;
		for (Item item : items.getItems()) {
			if (item == null)
				continue;
			w += ItemWeights.getWeight(item, false);
		}
		inventoryWeight = w;
		player.getPackets().refreshWeight(player.getEquipment().getEquipmentWeight() + inventoryWeight);
		player.getPackets().sendUpdateItems(93, items, slots);
	}

	public boolean addItem(int itemId, int amount) {
		if (itemId < 0 || amount < 0 || !CacheUtility.itemExists(itemId)
				)
			return false;
		Item[] itemsBefore = items.getItemsCopy();
		if (!items.add(new Item(itemId, amount))) {
			items.add(new Item(itemId, items.getFreeSlots()));
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			refreshItems(itemsBefore);
			return false;
		}
		refreshItems(itemsBefore);
		return true;
	}

	public boolean addItem(Item item) {
		if (item.getId() < 0 || item.getAmount() < 0 || !CacheUtility.itemExists(item.getId())
				|| ActivityHandler.execute(player, activity -> !activity.canAddInventoryItem(player, item.getId(), item.getAmount())))
			return false;
		Item[] itemsBefore = items.getItemsCopy();
		if (!items.add(item)) {
			items.add(new Item(item.getId(), items.getFreeSlots()));
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			refreshItems(itemsBefore);
			return false;
		}
		refreshItems(itemsBefore);
		return true;
	}

	public void deleteItem(int slot, Item item) {
		if (ActivityHandler.execute(player, activity -> !activity.canDeleteInventoryItem(player, item.getId(), item.getAmount())))
			return;
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(slot, item);
		refreshItems(itemsBefore);
	}

	public void removeItems(Item... list) {
		Array.of(list).filter(item -> item == null).forEach(item -> deleteItem(item));
	}

	public void deleteItem(int itemId, int amount) {
		if (ActivityHandler.execute(player, activity -> !activity.canDeleteInventoryItem(player, itemId, amount)))
			return;
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(new Item(itemId, amount));
		refreshItems(itemsBefore);
	}

	public void deleteItem(Item item) {
		if (ActivityHandler.execute(player, activity -> !activity.canDeleteInventoryItem(player, item.getId(), item.getAmount())))
			return;
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(item);
		refreshItems(itemsBefore);
	}

	/*
	 * No refresh needed its client to who does it :p
	 */
	public void switchItem(int fromSlot, int toSlot) {
		Item[] itemsBefore = items.getItemsCopy();
		Item fromItem = items.get(fromSlot);
		Item toItem = items.get(toSlot);
		items.set(fromSlot, toItem);
		items.set(toSlot, fromItem);
		refreshItems(itemsBefore);
	}

	public void refreshItems(Item[] itemsBefore) {
		byte[] changedSlots = new byte[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			if (itemsBefore[index] != items.getItems()[index])
				changedSlots[count++] = (byte) index;
		}
		byte[] finalChangedSlots = new byte[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
	}

	public boolean hasFreeSlots() {
		return getFreeSlots() != -1;
	}

	public int getFreeSlots() {
		return items.getFreeSlots();
	}

	public int getNumberOf(int itemId) {
		return items.getNumberOf(itemId);
	}

	public Item getItem(int slot) {
		return items.get(slot);
	}

	public int getItemsContainerSize() {
		return items.getSize();
	}
	
	public boolean isFull() {
		return getFreeSlots() == 0;
	}

	public boolean ifHasFreeSpace(int amount, Consumer<Player> consumer) {
		if (items.getFreeSlots() < amount) {
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			if (player.getActionManager().getAction() != null)
				player.getActionManager().forceStop();
			return false;
		}
		consumer.accept(player);
		return true;
	}
	
	public boolean containsItems(Item[] item) {
		for (int i = 0; i < item.length; i++)
			if (!items.contains(item[i]))
				return false;
		return true;
	}

	public boolean containsItems(int[] itemIds, int[] ammounts) {
		int size = itemIds.length > ammounts.length ? ammounts.length : itemIds.length;
		for (int i = 0; i < size; i++)
			if (!items.contains(new Item(itemIds[i], ammounts[i])))
				return false;
		return true;
	}
	
	public boolean contains(Item... items) {
		for (Item item : items) {
			if (item != null && !containsItem(item.getId(), item.getAmount())) {
				return false;
			}
		}
		return true;
	}

	public void replaceItem(Item item, Item item2) {
		deleteItem(item);
		addItem(item2);
	}
	
	public boolean addItems(Item... list) {
		Arrays.stream(list).forEach(item -> addItem(item));
		return true;
	}

	public boolean containsItem(int itemId, int ammount) {
		return items.contains(new Item(itemId, ammount));
	}

	public boolean containsOneItem(int... itemIds) {
		for (int itemId : itemIds) {
			if (items.containsOne(new Item(itemId, 1)))
				return true;
		}
		return false;
	}

	public void sendExamine(int slotId) {
		if (slotId >= getItemsContainerSize())
			return;
		Item item = items.get(slotId);
		if (item == null)
			return;
		player.getPackets().sendInventoryMessage(0, slotId, ItemExamines.getExamine(item));
	}

	public boolean hasRoomFor(Item[] deleting, Item... adding) {
		int freeSlots = getFreeSlots();
		int freedSlots = 0;
		if (deleting != null) {
			for (Item i : deleting) {
				if (i == null)
					continue;
				if (!i.getDefinitions().isStackable()
						|| (i.getDefinitions().isStackable() && getNumberOf(i.getId()) <= i.getAmount()))
					freedSlots++;
			}
		}
		freeSlots += freedSlots;
		int neededSlots = 0;
		for (Item i : adding) {
			if (!i.getDefinitions().isStackable()
					|| (i.getDefinitions().isStackable() && getNumberOf(i.getId()) <= 0))
				neededSlots++;
		}
		return freeSlots >= neededSlots;
	}

	private transient double inventoryWeight;
	
	public double getInventoryWeight() {
		return inventoryWeight;
	}
}