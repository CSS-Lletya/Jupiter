package com.jupiter.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.jupiter.cache.Cache;
import com.jupiter.cache.loaders.ObjectDefinitions;
import com.jupiter.cache.utility.CacheUtility;

public class ObjectListDumper {

	public static void main(String[] args) throws IOException {
		Cache.init();
		File file = new File("information/objectlist.txt");
		if (file.exists())
			file.delete();
		else
			file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.flush();
		for (int id = 0; id < CacheUtility.getObjectDefinitionsSize(); id++) {
			ObjectDefinitions def = ObjectDefinitions.getObjectDefinitions(id);
			writer.append(id + " - " + def.name);
			writer.newLine();
			System.out.println(id + " - " + def.name);
			writer.flush();
		}
		writer.close();
	}

}
