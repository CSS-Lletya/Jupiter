package com.jupiter.plugin.handlers;

import com.jupiter.cache.io.InputStream;
import com.jupiter.combat.npc.NPC;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.route.strategy.RouteEvent;
import com.jupiter.plugin.PluginManager;
import com.jupiter.plugin.events.NPCClickEvent;
import com.jupiter.utils.LogUtility;
import com.jupiter.utils.LogUtility.Type;

public abstract class NPCClickHandler extends PluginHandler<NPCClickEvent> {
	private boolean checkDistance = true;
	
	public NPCClickHandler(boolean checkDistance, Object[] namesOrIds) {
		super(namesOrIds);
		this.checkDistance = checkDistance;
	}
	
	public NPCClickHandler(Object... namesOrIds) {
		super(namesOrIds);
	}
	
	public NPCClickHandler(String[] options) {
		super(options);
	}

	public boolean isCheckDistance() {
		return checkDistance;
	}
	
	public static void executeMobInteraction(final Player player, InputStream stream, int optionId) {
		int npcIndex = stream.readUnsignedShort();//stream.readUnsignedShort128();
		boolean forceRun = stream.readUnsignedByte() == 1;//stream.read128Byte() == 1;
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished()
				|| !player.getMapRegionsIds().contains(npc.getRegionId()))
			return;
		player.getAttributes().stopAll(player, true);
		if (forceRun)
			player.setRun(forceRun);



		player.setRouteEvent(new RouteEvent(npc, new Runnable() {
			@Override
			public void run() {
				npc.resetWalkSteps();
				player.faceEntity(npc);
				PluginManager.handle(new NPCClickEvent(player, npc, optionId));
			}
		}, npc.getDefinitions().name.contains("Banker") || npc.getDefinitions().name.contains("banker")));

	}
	
	public static void handleExamine(final Player player, InputStream stream) {
		int npcIndex = stream.readUnsignedShort();//stream.readUnsignedShort128();
		boolean forceRun = stream.readUnsignedByte() == 1;//stream.read128Byte() == 1;
		if (forceRun)
			player.setRun(forceRun);
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()))
			return;
		if (player.getPlayerDetails().getRights().isStaff()) {
			player.getPackets().sendGameMessage("NPC - [id=" + npc.getId() + ", loc=[" + npc.getX() + ", " + npc.getY()
					+ ", " + npc.getPlane() + "]].");
		}
		player.getPackets().sendNPCMessage(0, npc, "It's a " + npc.getDefinitions().name + ".");
		LogUtility.log(Type.INFO, "NPC Click Handler", "examined npc: " + npcIndex + ", " + npc.getId());
	}
}