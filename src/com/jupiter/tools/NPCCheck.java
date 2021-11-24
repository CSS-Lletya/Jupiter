package com.jupiter.tools;

import java.io.IOException;

import com.jupiter.cache.Cache;
import com.jupiter.cache.loaders.NPCDefinitions;
import com.jupiter.utils.Utils;

public class NPCCheck {

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (int id = 0; id < Utils.getNPCDefinitionsSize(); id++) {
			NPCDefinitions def = NPCDefinitions.getNPCDefinitions(id);
			if (def.name.contains("Elemental")) {
				System.out.println(id + " - " + def.name);
			}
		}
	}

}
