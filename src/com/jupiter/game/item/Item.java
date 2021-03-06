package com.jupiter.game.item;

import java.util.HashMap;
import java.util.Map;

import com.jupiter.cache.loaders.ItemDefinitions;
import com.jupiter.utility.Utility;

import lombok.Data;

@Data
public class Item {

	private int slot;
	private int id;
	private int amount;
	private Map<String, Object> metaData;

	public int getId() {
		return id;
	}

	@Override
	public Item clone() {
		return new Item(id, amount, Utility.cloneMap(metaData));
	}
	
	public Item(Item item) {
		this(item.getId(), item.getAmount(), Utility.cloneMap(item.getMetaData()));
	}

	public Item(int id) {
		this(id, 1);
	}

	public Item(int id, int amount) {
		this(id, amount, false);
	}

	public Item(int id, int amount, Map<String, Object> metaData) {
		this(id, amount);
		this.metaData = metaData;
	}

	public Item(int id, int amount, boolean amt0) {
		this.id = (short) id;
		this.amount = amount;
		if (this.amount <= 0 && !amt0) {
			this.amount = 1;
		}
	}

	public ItemDefinitions getDefinitions() {
		return ItemDefinitions.getItemDefinitions(id);
	}

	public int getEquipId() {
		return getDefinitions().getEquipId();
	}

	public Item setAmount(int amount) {
		if (amount < 0 || amount > Integer.MAX_VALUE)
			return this;
		this.amount = amount;
		return this;
	}

	public String getName() {
		return getDefinitions().getName();
	}
	
	public Item addMetaData(String key, Object value) {
		if (metaData == null)
			metaData = new HashMap<String, Object>();
		metaData.put(key, value);
		return this;
	}
	
	public Object getMetaData(String key) {
		if (metaData != null)
			return metaData.get(key);
		return null;
	}
	
	public int incMetaDataI(String key) {
		int val = getMetaDataI(key) + 1;
		addMetaData(key, val);
		return val;
	}
	
	public int decMetaDataI(String key) {
		int val = getMetaDataI(key) - 1;
		addMetaData(key, val);
		return val;
	}
	
	public int getMetaDataI(String key) {
		return getMetaDataI(key, -1);
	}
	
	public int getMetaDataI(String key, int defaultVal) {
		if (metaData != null && metaData.get(key) != null) {
			if (metaData.get(key) instanceof Integer)
				return (int) metaData.get(key);
			return (int) Math.floor(((double) metaData.get(key)));
		}
		return defaultVal;
	}

	public void deleteMetaData() {
		this.metaData = null;
	}
	
	@Override
	public String toString() {
		return "[" + ItemDefinitions.getItemDefinitions(id).name + " ("+id+"), " + amount + "]";
	}

	public boolean containsMetaData() {
		return metaData != null;
	}

	public double getMetaDataD(String key, double defaultVal) {
		if (metaData != null) {
			if (metaData.get(key) != null);
				return (double) metaData.get(key);
		}
		return defaultVal;
	}
	
	public double getMetaDataD(String key) {
		return getMetaDataD(key, 0);
	}
}