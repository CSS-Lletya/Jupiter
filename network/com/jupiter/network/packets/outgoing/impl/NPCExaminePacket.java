package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.combat.npc.NPC;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.LogUtility.Type;

@OutgoingPacketSignature(packetId = 3, description = "Represents a Player's AFK state")
public class NPCExaminePacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
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