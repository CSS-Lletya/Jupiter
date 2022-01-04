package com.jupiter.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.jupiter.cache.Cache;
import com.jupiter.cache.loaders.ItemDefinitions;
import com.jupiter.cache.utility.CacheUtility;

public class ItemListDumper {

	public static void main(String[] args) {
		try {
			new ItemListDumper();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ItemListDumper() throws IOException {
		Cache.init();
		File file = new File("itemList.txt");
		if (file.exists())
			file.delete();
		else
			file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.flush();
		for (int id = 0; id < CacheUtility.getItemDefinitionsSize(); id++) {
			ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
			if (def.getName().equals("null"))
				continue;
			writer.append(id + " - " + def.getName());
			writer.newLine();
			writer.flush();
		}
		writer.close();
	}

	public static int convertInt(String str) {
		try {
			int i = Integer.parseInt(str);
			return i;
		} catch (NumberFormatException e) {
		}
		return 0;
	}

}
