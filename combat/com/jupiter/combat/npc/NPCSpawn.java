package com.jupiter.combat.npc;

import com.jupiter.game.map.WorldTile;

public class NPCSpawn {

	private String comment;
	private int npcId;
	private WorldTile tile;
	
	public NPCSpawn(int npcId, WorldTile tile, String comment) {
		this.npcId = npcId;
		this.tile = tile;
		this.comment = comment;
	}
	
	public void spawn() {
		NPC.spawnNPC(npcId, tile, false, false);
	}
	
	public WorldTile getTile() {
		return tile;
	}
	
	public int getNPCId() {
		return npcId;
	}

	public String getComment() {
		return comment;
	}
}
