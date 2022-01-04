package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.cache.utility.CacheUtility;
import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.game.item.Item;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.activity.ActivityHandler;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.plugin.PluginManager;
import com.jupiter.plugin.events.ItemOnPlayerEvent;
import com.jupiter.skills.magic.Magic;
import com.jupiter.utility.Utility;

@LogicPacketSignature(packetId = 13, packetSize = 11, description = "An Interface that's used onto a Player (Magic, etc..)")
public class InterfaceOnPlayerPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
			return;
		if (player.getMovement().getLockDelay() > Utility.currentTimeMillis())
			return;
		
		int slot = stream.readUnsignedShort();
		int playerIndex = stream.readUnsignedShortLE();
		boolean forceRun = stream.readUnsigned128Byte() == 1;
		int interfaceHash = stream.readIntV2();
		int itemId = stream.readUnsignedShortLE();
		
		int interfaceId = interfaceHash >> 16;
		int componentId = interfaceHash - (interfaceId << 16);
		
		System.out.println(String.format("%s, %s, %s, %s, %s,", slot, playerIndex, forceRun, interfaceHash, itemId));
		System.out.println(String.format("%s, %s,", interfaceId, componentId));
		
		
		if (CacheUtility.getInterfaceDefinitionsSize() <= interfaceId)
			return;
		if (!player.getInterfaceManager().containsInterface(interfaceId))
			return;
		if (componentId == 65535)
			componentId = -1;
		if (componentId != -1 && CacheUtility.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId)
			return;
		Player p2 = World.getPlayers().get(playerIndex);
		if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
			return;
		player.getAttributes().stopAll(player, false);
		if (forceRun)
			player.setRun(forceRun);
		switch (interfaceId) {
		case 662:
		case 747:
			player.resetWalkSteps();
			if ((interfaceId == 747 && componentId == 15) || (interfaceId == 662 && componentId == 65) || (interfaceId == 662 && componentId == 74) || interfaceId == 747 && componentId == 18) {
				if (!player.isCanPvp() || !p2.isCanPvp()) {
					player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
					return;
				}
			}
			break;
		case 193:
			switch (componentId) {
			case 28:
			case 32:
			case 24:
			case 20:
			case 30:
			case 34:
			case 26:
			case 22:
			case 29:
			case 33:
			case 25:
			case 21:
			case 31:
			case 35:
			case 27:
			case 23:
				if (Magic.checkCombatSpell(player, componentId, 1, false)) {
					player.setNextFaceWorldTile(new WorldTile(p2.getCoordFaceX(p2.getSize()), p2.getCoordFaceY(p2.getSize()), p2.getPlane()));
					if (ActivityHandler.execute(player, activity -> !activity.canAttack(player, p2)))
						return;
					if (!player.isCanPvp() || !p2.isCanPvp()) {
						player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
						return;
					}
					if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
						if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utility.currentTimeMillis()) {
							player.getPackets().sendGameMessage("That " + (player.getAttackedBy() instanceof Player ? "player" : "npc") + " is already in combat.");
							return;
						}
						if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utility.currentTimeMillis()) {
							if (p2.getAttackedBy() instanceof NPC) {
								p2.setAttackedBy(player); // changes enemy
								// to player,
								// player has
								// priority over
								// npc on single
								// areas
							} else {
								player.getPackets().sendGameMessage("That player is already in combat.");
								return;
							}
						}
					}
					player.getActionManager().setAction(new PlayerCombat(p2));
				}
				break;
			}
		case 192:
			switch (componentId) {
			case 25: // air strike
			case 28: // water strike
			case 30: // earth strike
			case 32: // fire strike
			case 34: // air bolt
			case 39: // water bolt
			case 42: // earth bolt
			case 45: // fire bolt
			case 49: // air blast
			case 52: // water blast
			case 58: // earth blast
			case 63: // fire blast
			case 70: // air wave
			case 73: // water wave
			case 77: // earth wave
			case 80: // fire wave
			case 86: // teleblock
			case 84: // air surge
			case 87: // water surge
			case 89: // earth surge
			case 91: // fire surge
			case 99: // storm of armadyl
			case 36: // bind
			case 66: // Sara Strike
			case 67: // Guthix Claws
			case 68: // Flame of Zammy
			case 55: // snare
			case 81: // entangle
				if (Magic.checkCombatSpell(player, componentId, 1, false)) {
					player.setNextFaceWorldTile(new WorldTile(p2.getCoordFaceX(p2.getSize()), p2.getCoordFaceY(p2.getSize()), p2.getPlane()));
					if (ActivityHandler.execute(player, activity -> !activity.canAttack(player, p2)))
						return;
					if (!player.isCanPvp() || !p2.isCanPvp()) {
						player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
						return;
					}
					if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
						if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utility.currentTimeMillis()) {
							player.getPackets().sendGameMessage("That " + (player.getAttackedBy() instanceof Player ? "player" : "npc") + " is already in combat.");
							return;
						}
						if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utility.currentTimeMillis()) {
							if (p2.getAttackedBy() instanceof NPC) {
								p2.setAttackedBy(player); // changes enemy
								// to player,
								// player has
								// priority over
								// npc on single
								// areas
							} else {
								player.getPackets().sendGameMessage("That player is already in combat.");
								return;
							}
						}
					}
					player.getActionManager().setAction(new PlayerCombat(p2));
				}
				break;
			}
			break;
		}
		if (PluginManager.handle(new ItemOnPlayerEvent(player, p2, new Item(itemId)))) {
			return;
		}
		
			System.out.println("Spell:" + componentId);
	}
}