package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.cache.utility.CacheUtility;
import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.game.item.Item;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Inventory;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.activity.ActivityHandler;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.plugins.rsinterface.impl.InventoryInterfacePlugin;
import com.jupiter.skills.magic.Magic;
import com.jupiter.utility.Utility;

@LogicPacketSignature(packetId = 41, packetSize = 11, description = "An Interface that's used onto a NPC (Magic, etc..)")
public class InterfaceOnNPCPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
			return;
		if (player.getMovement().getLockDelay() > Utility.currentTimeMillis())
			return;

		int interfaceHash = stream.readIntV2();
		int npcIndex = stream.readUnsignedShortLE128();
		boolean forceRun = stream.readUnsigned128Byte() == 1;
		int itemId = stream.readUnsignedShortLE128();
		int interfaceSlot = stream.readUnsignedShort128();

		int interfaceId = interfaceHash >> 16;
		int componentId = interfaceHash - (interfaceId << 16);

		System.out.println(
				String.format("%s, %s, %s, %s, %s,", interfaceHash, npcIndex, forceRun, itemId, interfaceSlot));
		System.out.println(String.format("%s, %s,", interfaceId, componentId));

		if (CacheUtility.getInterfaceDefinitionsSize() <= interfaceId)
			return;
		if (!player.getInterfaceManager().containsInterface(interfaceId))
			return;
		if (componentId == 65535)
			componentId = -1;
		if (componentId != -1 && CacheUtility.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId)
			return;
		NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()))
			return;
		player.getAttributes().stopAll(player, false);
		if (forceRun)
			player.setRun(forceRun);
		if (interfaceId != Inventory.INVENTORY_INTERFACE) {
			if (!npc.getDefinitions().hasAttackOption()) {
				player.getPackets().sendGameMessage("You can't attack this npc.");
				return;
			}
		}
		switch (interfaceId) {
		case Inventory.INVENTORY_INTERFACE:
			Item item = player.getInventory().getItem(interfaceSlot);
			if (item == null
					|| ActivityHandler.execute(player, activity -> !activity.processItemOnNPC(player, npc, item)))
				return;
			InventoryInterfacePlugin.handleItemOnNPC(player, npc, item);
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
					player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()),
							npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
					if (ActivityHandler.execute(player, activity -> !activity.canAttack(player, npc)))
						return;
					if (!npc.isForceMultiAttacked()) {
						if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
							if (player.getAttackedBy() != npc
									&& player.getAttackedByDelay() > Utility.currentTimeMillis()) {
								player.getPackets().sendGameMessage("You are already in combat.");
								return;
							}
							if (npc.getAttackedBy() != player
									&& npc.getAttackedByDelay() > Utility.currentTimeMillis()) {
								player.getPackets().sendGameMessage("This npc is already in combat.");
								return;
							}
						}
					}
					player.getActionManager().setAction(new PlayerCombat(npc));
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
			case 84: // air surge
			case 87: // water surge
			case 89: // earth surge
			case 66: // Sara Strike
			case 67: // Guthix Claws
			case 68: // Flame of Zammy
			case 93:
			case 91: // fire surge
			case 99: // storm of Armadyl
			case 36: // bind
			case 55: // snare
			case 81: // entangle
				if (Magic.checkCombatSpell(player, componentId, 1, false)) {
					player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()),
							npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
					if (ActivityHandler.execute(player, activity -> !activity.canAttack(player, npc)))
						return;
					if (!npc.isForceMultiAttacked()) {
						if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
							if (player.getAttackedBy() != npc
									&& player.getAttackedByDelay() > Utility.currentTimeMillis()) {
								player.getPackets().sendGameMessage("You are already in combat.");
								return;
							}
							if (npc.getAttackedBy() != player
									&& npc.getAttackedByDelay() > Utility.currentTimeMillis()) {
								player.getPackets().sendGameMessage("This npc is already in combat.");
								return;
							}
						}
					}
					player.getActionManager().setAction(new PlayerCombat(npc));
				}
				break;
			}
			break;
		}

		System.out.println("Spell:" + componentId);
	}
}