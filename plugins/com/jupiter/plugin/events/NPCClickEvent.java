package com.jupiter.plugin.events;

import java.util.HashMap;
import java.util.Map;

import com.jupiter.combat.npc.NPC;
import com.jupiter.game.player.Player;
import com.jupiter.plugin.handlers.NPCClickHandler;
import com.jupiter.plugin.handlers.PluginHandler;

public class NPCClickEvent implements PluginEvent {
	
	private static Map<Object, NPCClickHandler> HANDLERS = new HashMap<>();

	private Player player;
	private NPC npc;
	private int opNum;
	private String option;

	public NPCClickEvent(Player player, NPC npc, int opNum) {
		this.player = player;
		this.npc = npc;
		this.opNum = opNum;
	}

	public Player getPlayer() {
		return player;
	}

	public NPC getNPC() {
		return npc;
	}
	
	public int getNPCId() {
		return npc.getId();
	}

	public String getOption() {
		return option;
	}
	
	public int getOpNum() {
		return opNum;
	}

	@Override
	public PluginHandler<? extends PluginEvent> getMethod() {
		NPCClickHandler method = HANDLERS.get(getNPC().getId());
		if (method == null)
			method = HANDLERS.get(getNPC().getDefinitions().getName());
		if (method == null)
			method = HANDLERS.get(getOption());
		if (method == null)
			return null;
		return method;
	}

	public static void registerMethod(Class<?> eventType, PluginHandler<? extends PluginEvent> method) {
		for (Object key : method.keys()) {
			PluginHandler<? extends PluginEvent> old = HANDLERS.put(key, (NPCClickHandler) method);
			if (old != null) {
				System.err.println("ERROR: Duplicate NPCClick methods for key: " + key);
			}
		}
	}

}
