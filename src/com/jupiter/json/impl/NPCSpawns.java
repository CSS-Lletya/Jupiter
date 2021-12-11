package com.jupiter.json.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonIOException;
import com.jupiter.combat.npc.NPCSpawn;
import com.jupiter.json.GsonLoader;
import com.jupiter.utils.Logger;

import io.vavr.collection.Array;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class NPCSpawns {

	private final static String PATH = "data/npcs/spawns/";
	final static Charset ENCODING = StandardCharsets.UTF_8;

	private static final ObjectArrayList<NPCSpawn> ALL_SPAWNS = new ObjectArrayList<>();
	private static final Object2ObjectArrayMap<Integer, List<NPCSpawn>> NPC_SPAWNS = new Object2ObjectArrayMap<>();

	public static final void init() throws JsonIOException, IOException {
		Logger.log("NPCSpawns", "Loading NPC spawns...");
		File[] spawnFiles = new File(PATH).listFiles();
		for (File files : spawnFiles) {
			NPCSpawn[] spawns = (NPCSpawn[]) GsonLoader.loadJsonFile(files, NPCSpawn[].class);
			Array.of(spawns).filter(spawn -> spawn != null).forEach(spawn -> add(spawn));
		}
		Logger.log("NPCSpawns", "Loaded " + ALL_SPAWNS.size() + " NPC spawns...");
	}

	public static void add(NPCSpawn spawn) {
		ALL_SPAWNS.add(spawn);
		List<NPCSpawn> regionSpawns = NPC_SPAWNS.get(spawn.getTile().getRegionId());
		if (regionSpawns == null)
			regionSpawns = new ArrayList<>();
		regionSpawns.add(spawn);
		NPC_SPAWNS.put(spawn.getTile().getRegionId(), regionSpawns);
	}

	public static void loadNPCSpawns(int regionId) {
		Array.of(NPC_SPAWNS.get(regionId)).filter(spawn -> spawn != null).forEach(npc -> npc.forEach(mob -> mob.spawn()));
	}
}