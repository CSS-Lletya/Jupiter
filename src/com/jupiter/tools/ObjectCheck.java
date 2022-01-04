package com.jupiter.tools;

import java.io.IOException;

import com.jupiter.cache.Cache;
import com.jupiter.cache.loaders.ObjectDefinitions;
import com.jupiter.cache.utility.CacheUtility;

public class ObjectCheck {

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (int i = 0; i < CacheUtility.getObjectDefinitionsSize(); i++) {
			ObjectDefinitions def = ObjectDefinitions.getObjectDefinitions(i);
			if (def.containsOption("Steal-from")) {
				System.out.println(def.id + " - " + def.name);
			}
		}
	}

}
