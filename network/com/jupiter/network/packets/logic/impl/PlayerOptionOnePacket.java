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

@LogicPacketSignature(packetId = 66, packetSize = 3, description = "The First menu option for a Player")
public class PlayerOptionOnePacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
			return;
		int playerIndex = stream.readUnsignedShort(); //incorrect returns 32k
		boolean forceRun = stream.read128Byte() == 1;
		if (forceRun)
			player.setRun(true);
		Player targetPlayer = World.getPlayers().get(playerIndex);
		if (targetPlayer == null || targetPlayer.isDead() || targetPlayer.hasFinished()
				|| !player.getMapRegionsIds().contains(targetPlayer.getRegionId()))
			return;
		if (targetPlayer == null || targetPlayer.isDead() || targetPlayer.hasFinished() || !player.getMapRegionsIds().contains(targetPlayer.getRegionId()))
			return;
		
		if (player.getMovement().getLockDelay() > Utility.currentTimeMillis() || ActivityHandler.execute(player, activity -> !activity.canPlayerOption1(player, targetPlayer)))
			return;
		if (!player.isCanPvp())
			return;
		if (ActivityHandler.execute(player, activity -> !activity.canAttack(player, targetPlayer)))
			return;
		
		if (!player.isCanPvp() || !targetPlayer.isCanPvp()) {
			player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
			return;
		}
		if (!targetPlayer.isAtMultiArea() || !player.isAtMultiArea()) {
			if (player.getAttackedBy() != targetPlayer && player.getAttackedByDelay() > Utility.currentTimeMillis()) {
				player.getPackets().sendGameMessage("You are already in combat.");
				return;
			}
			if (targetPlayer.getAttackedBy() != player && targetPlayer.getAttackedByDelay() > Utility.currentTimeMillis()) {
				if (targetPlayer.getAttackedBy() instanceof NPC) {
					targetPlayer.setAttackedBy(player); // changes enemy to player,
					// player has priority over
					// npc on single areas
				} else {
					player.getPackets().sendGameMessage("That player is already in combat.");
					return;
				}
			}
		}
		
		player.getAttributes().stopAll(player, false);
		player.getActionManager().setAction(new PlayerCombat(targetPlayer));
	}
}