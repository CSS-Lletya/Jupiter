package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.activity.ActivityHandler;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.utility.Utility;

@LogicPacketSignature(packetId = 16, packetSize = 3, description = "Attack an NPC")
public class AttackNPCPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead()) {
			return;
		}
		if (player.getMovement().getLockDelay() > Utility.currentTimeMillis()) {
			return;
		}
		int npcIndex = stream.readUnsignedShort();//stream.readUnsignedShort128();
		boolean forceRun = stream.readUnsignedByte() == 1;//stream.read128Byte() == 1;
		if (forceRun)
			player.setRun(forceRun);
		NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()) || !npc.getDefinitions().hasAttackOption()) {
			return;
		}
		if (ActivityHandler.execute(player, activity -> !activity.canAttack(player, npc))) {
			return;
		}
		if (!npc.isForceMultiAttacked()) {
			if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
				if (player.getAttackedBy() != npc && player.getAttackedByDelay() > Utility.currentTimeMillis()) {
					player.getPackets().sendGameMessage("You are already in combat.");
					return;
				}
				if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utility.currentTimeMillis()) {
					player.getPackets().sendGameMessage("This npc is already in combat.");
					return;
				}
			}
		}
		player.getAttributes().stopAll(player, false);
		player.getActionManager().setAction(new PlayerCombat(npc));
	}
}