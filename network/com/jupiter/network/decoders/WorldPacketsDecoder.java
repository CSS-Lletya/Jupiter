package com.jupiter.network.decoders;

import java.util.stream.IntStream;

import com.jupiter.cache.io.InputStream;
import com.jupiter.cache.utility.CacheUtility;
import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.game.item.FloorItem;
import com.jupiter.game.item.Item;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Inventory;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.actions.PlayerFollow;
import com.jupiter.game.player.activity.ActivityHandler;
import com.jupiter.game.route.RouteFinder;
import com.jupiter.game.route.strategy.FixedTileStrategy;
import com.jupiter.game.route.strategy.RouteEvent;
import com.jupiter.network.Session;
import com.jupiter.network.packets.outgoing.OutgoingPacketDispatcher;
import com.jupiter.plugin.PluginManager;
import com.jupiter.plugin.events.ItemOnObjectEvent;
import com.jupiter.plugin.events.ItemOnPlayerEvent;
import com.jupiter.plugin.handlers.NPCClickHandler;
import com.jupiter.plugin.handlers.ObjectClickHandler;
import com.jupiter.plugins.rsinterface.impl.InventoryInterfacePlugin;
import com.jupiter.skills.magic.Magic;
import com.jupiter.utility.Utility;

public final class WorldPacketsDecoder extends Decoder {

	private static final byte[] PACKET_SIZES = new byte[104];

	//WALKING
	private final static int WALKING_PACKET = 33;
	private final static int MINI_WALKING_PACKET = 42;
	
	//BUTTONS
	public final static int ACTION_BUTTON1_PACKET = 96;
	public final static int ACTION_BUTTON2_PACKET = 27;
	public final static int ACTION_BUTTON3_PACKET = 68;
	public final static int ACTION_BUTTON4_PACKET = 9;
	public final static int ACTION_BUTTON5_PACKET = 72;
	public final static int ACTION_BUTTON6_PACKET = 19;
	public final static int ACTION_BUTTON7_PACKET = 23;
	public final static int ACTION_BUTTON8_PACKET = 21;
	public final static int ACTION_BUTTON9_PACKET = 22;
	public final static int ACTION_BUTTON10_PACKET = 81;
	
	//PLAYER BUTTONS
	private final static int PLAYER_OPTION_1_PACKET = 66;
	private final static int PLAYER_OPTION_2_PACKET = 6;
	private final static int PLAYER_OPTION_3_PACKET = 31;
	private final static int PLAYER_OPTION_4_PACKET = 89;
//	private final static int PLAYER_OPTION_5_PACKET = 103;
//	private final static int PLAYER_OPTION_6_PACKET = 1;
//	private final static int PLAYER_OPTION_7_PACKET = 51;
//	private final static int PLAYER_OPTION_8_PACKET = 94;
	private final static int PLAYER_OPTION_9_PACKET = 53;
	private final static int PLAYER_OPTION_10_PACKET = 70;
	
	//OBJECT BUTTONS
	private final static int OBJECT_CLICK1_PACKET = 75;
	private final static int OBJECT_CLICK2_PACKET = 93;
	private final static int OBJECT_CLICK3_PACKET = 38;
	private final static int OBJECT_CLICK4_PACKET = 32;
	private final static int OBJECT_CLICK5_PACKET = 48;
	
	
	//NPC BUTTONS
	private final static int ATTACK_NPC = 16;
	private final static int NPC_CLICK1_PACKET = 65;
	private final static int NPC_CLICK2_PACKET = 50;
	private final static int NPC_CLICK3_PACKET = 77;
	private final static int NPC_CLICK4_PACKET = 95;
	
	private static final int GRAND_EXCHANGE_PACKET = 17;
	
	//CHAT TYPES
	@SuppressWarnings("unused")
	private final static int PUBLIC_QUICK_CHAT_PACKET = 64;
	
	@SuppressWarnings("unused")
	private final static int SEND_FRIEND_QUICK_CHAT_PACKET = 14;
	
	private final static int ITEM_TAKE_PACKET = 54;
 
	private final static int INTERFACE_ON_OBJECT = 98;
	private final static int INTERFACE_ON_PLAYER = 13;
	private final static int INTERFACE_ON_NPC = 41;
	public final static int WORLD_MAP_CLICK = 5;
	public final static int RECEIVE_PACKET_COUNT_PACKET = -1;
	

	public static void loadPacketSizes() {
		PACKET_SIZES[0] = 0;
		PACKET_SIZES[1] = 3;
		PACKET_SIZES[2] = 4;
		PACKET_SIZES[3] = 3;
		PACKET_SIZES[4] = 16;
		PACKET_SIZES[5] = 4;
		PACKET_SIZES[6] = 3;
		PACKET_SIZES[7] = -1;
		PACKET_SIZES[8] = 7;
		PACKET_SIZES[9] = 8;
		PACKET_SIZES[10] = -1;
		PACKET_SIZES[11] = 2;
		PACKET_SIZES[12] = -1;
		PACKET_SIZES[13] = 11;
		PACKET_SIZES[14] = -1;
		PACKET_SIZES[15] = -2;
		PACKET_SIZES[16] = 3;
		PACKET_SIZES[17] = 2;
		PACKET_SIZES[18] = 4;
		PACKET_SIZES[19] = 8;
		PACKET_SIZES[20] = 3;
		PACKET_SIZES[21] = 8;
		PACKET_SIZES[22] = 8;
		PACKET_SIZES[23] = 8;
		PACKET_SIZES[24] = 7;
		PACKET_SIZES[25] = 7;
		PACKET_SIZES[26] = -1;
		PACKET_SIZES[27] = 8;
		PACKET_SIZES[28] = -2;
		PACKET_SIZES[29] = -1;
		PACKET_SIZES[30] = 1;
		PACKET_SIZES[31] = 3;
		PACKET_SIZES[32] = 9;
		PACKET_SIZES[33] = 5;
		PACKET_SIZES[34] = -1;
		PACKET_SIZES[35] = -2;
		PACKET_SIZES[36] = -1;
		PACKET_SIZES[37] = 2;
		PACKET_SIZES[38] = 9;
		PACKET_SIZES[39] = -1;
		PACKET_SIZES[40] = -1;
		PACKET_SIZES[41] = 11;
		PACKET_SIZES[42] = 18;
		PACKET_SIZES[43] = 7;
		PACKET_SIZES[44] = 9;
		PACKET_SIZES[45] = 1;
		PACKET_SIZES[46] = 12;
		PACKET_SIZES[47] = 4;
		PACKET_SIZES[48] = 9;
		PACKET_SIZES[49] = 6;
		PACKET_SIZES[50] = 3;
		PACKET_SIZES[51] = 3;
		PACKET_SIZES[52] = -1;
		PACKET_SIZES[53] = 3;
		PACKET_SIZES[54] = 7;
		PACKET_SIZES[55] = 4;
		PACKET_SIZES[56] = -2;
		PACKET_SIZES[57] = 7;
		PACKET_SIZES[58] = 4;
		PACKET_SIZES[59] = 6;
		PACKET_SIZES[60] = 0;
		PACKET_SIZES[61] = 7;
		PACKET_SIZES[62] = 1;
		PACKET_SIZES[63] = 4;
		PACKET_SIZES[64] = -1;
		PACKET_SIZES[65] = 3;
		PACKET_SIZES[66] = 3;
		PACKET_SIZES[67] = 15;
		PACKET_SIZES[68] = 8;
		PACKET_SIZES[69] = -1;
		PACKET_SIZES[70] = 3;
		PACKET_SIZES[71] = -1;
		PACKET_SIZES[72] = 8;
		PACKET_SIZES[73] = 9;
		PACKET_SIZES[74] = 16;
		PACKET_SIZES[75] = 9;
		PACKET_SIZES[76] = 0;
		PACKET_SIZES[77] = 3;
		PACKET_SIZES[78] = -1;
		PACKET_SIZES[79] = 1;
		PACKET_SIZES[80] = -1;
		PACKET_SIZES[81] = 8;
		PACKET_SIZES[82] = 4;
		PACKET_SIZES[83] = 4;
		PACKET_SIZES[84] = 6;
		PACKET_SIZES[85] = -1;
		PACKET_SIZES[86] = -1;
		PACKET_SIZES[87] = -1;
		PACKET_SIZES[88] = 2;
		PACKET_SIZES[89] = 3;
		PACKET_SIZES[90] = -1;
		PACKET_SIZES[91] = -1;
		PACKET_SIZES[92] = -2;
		PACKET_SIZES[93] = 9;
		PACKET_SIZES[94] = 3;
		PACKET_SIZES[95] = 3;
		PACKET_SIZES[96] = 8;
		PACKET_SIZES[97] = -1;
		PACKET_SIZES[98] = 17;
		PACKET_SIZES[99] = -2;
		PACKET_SIZES[100] = -1;
		PACKET_SIZES[101] = -2;
		PACKET_SIZES[102] = -2;
		PACKET_SIZES[103] = 3;
	}

	private Player player;

	public WorldPacketsDecoder(Session session, Player player) {
		super(session);
		this.player = player;
	}

	@Override
	public void decode(InputStream stream) {
		while (stream.getRemaining() > 0 && session.getChannel().isConnected() && !player.hasFinished()) {
			int packetId = stream.readPacket(player);
			if (packetId >= PACKET_SIZES.length || packetId < 0) {
					System.out.println("PacketId " + packetId + " has fake packet id.");
				break;
			}
			int finalLength;
			int length = PACKET_SIZES[packetId];
			if (length == -1)
				length = stream.readUnsignedByte();
			else if (length == -2)
				length = stream.readUnsignedShort();
			else if (length == -3)
				length = stream.readInt();
			else if (length == -4) {
				length = stream.getRemaining();
				
					System.out.println("Invalid size for PacketId " + packetId + ". Size guessed to be " + length);
			}
			finalLength = length;
			if (length > stream.getRemaining()) {
				length = stream.getRemaining();
					System.out.println("PacketId " + packetId + " has fake size. - expected size " + length);
				// break;

			}

			if (packetId < 0) {
				System.out.println("PacketId " + packetId + " has . - expected size " + length);
			}

			int startOffset = stream.getOffset();
			IntStream.of(LOGICAL_PACKETS).filter(size -> size == packetId).forEach(packet -> player.addLogicPacketToQueue(new LogicPacket(packetId, finalLength, stream)));
			OutgoingPacketDispatcher.execute(player, stream, packetId);
			stream.setOffset(startOffset + length);
		}
	}

	private final int[] LOGICAL_PACKETS = {
			WALKING_PACKET,MINI_WALKING_PACKET,ITEM_TAKE_PACKET,PLAYER_OPTION_1_PACKET,PLAYER_OPTION_2_PACKET,PLAYER_OPTION_3_PACKET,PLAYER_OPTION_4_PACKET,PLAYER_OPTION_9_PACKET,PLAYER_OPTION_10_PACKET, PLAYER_OPTION_9_PACKET,
			ATTACK_NPC,INTERFACE_ON_PLAYER,INTERFACE_ON_NPC,NPC_CLICK1_PACKET,NPC_CLICK2_PACKET,NPC_CLICK3_PACKET,NPC_CLICK4_PACKET, 
			OBJECT_CLICK1_PACKET,OBJECT_CLICK2_PACKET,OBJECT_CLICK3_PACKET,OBJECT_CLICK4_PACKET,INTERFACE_ON_OBJECT
	};
	
	public static void decodeLogicPacket(final Player player, LogicPacket packet) {
		int packetId = packet.getId();
		InputStream stream = new InputStream(packet.getData());
		if (packetId == WALKING_PACKET || packetId == MINI_WALKING_PACKET) {
			if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
				return;
			long currentTime = Utility.currentTimeMillis();
			if (player.getMovement().getLockDelay() > currentTime)
				return;
			if (player.getFreezeDelay() >= currentTime) {
				player.getPackets().sendGameMessage("A magical force prevents you from moving.");
				return;
			}
			@SuppressWarnings("unused")
			int length = stream.getLength();
			/*
			 * if (packetId == MINI_WALKING_PACKET) length -= 13;
			 */
			
			boolean forceRun = stream.readUnsignedByte() == 1;
			if (forceRun)
				player.setRun(forceRun);
			int baseX = stream.readUnsignedShort();
			int baseY = stream.readUnsignedShortLE();
	
            int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(), player.getPlane(), player.getSize(), new FixedTileStrategy(baseX, baseY), true);
			if (steps > 25)
	            steps = 25;

	        player.getAttributes().stopAll(player);

	        player.setNextFaceEntity(null);
	        
	        player.getAction().ifPresent(skill -> skill.cancel());

	        if (steps > 0) {

	            @SuppressWarnings("unused")
				int x = 0, y = 0;
	            for (int step = 0; step < steps; step++) {
	                x = baseX + stream.readUnsignedByte();
	                y = baseY + stream.readUnsignedByte();
	            }
	            
	            int[] bufferX = RouteFinder.getLastPathBufferX();
	            int[] bufferY = RouteFinder.getLastPathBufferY();
	            int last = -1;
	            for (int i = steps - 1; i >= 0; i--) {
	                if (!player.addWalkSteps(bufferX[i], bufferY[i], 25, true))
	                    break;
	                last = i;
	            }

	            if (last != -1) {
	                WorldTile tile = new WorldTile(bufferX[last], bufferY[last], player.getPlane());
	                player.getPackets().sendMinimapFlag(tile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize()), tile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize()));
	            } else {
	                player.getPackets().sendResetMinimapFlag();
	            }
	        }
		} 
		//TODO do player buttons here
		
//		switch (packetId){
//		case PLAYER_OPTION_1_PACKET:
//		case PLAYER_OPTION_2_PACKET:
//		case PLAYER_OPTION_3_PACKET:
//		case PLAYER_OPTION_4_PACKET:
//		case PLAYER_OPTION_5_PACKET:
//		case PLAYER_OPTION_6_PACKET:
//		case PLAYER_OPTION_7_PACKET:
//		case PLAYER_OPTION_8_PACKET:
//		
//			int playerIndex = stream.readUnsignedShort(); //incorrect returns 32k
//			boolean forceRun = stream.read128Byte() == 1;
//			if (forceRun)
//				player.setRun(true);
//			Player p2 = World.getPlayers().get(playerIndex);
//			if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
//				return;
//			player.getPackets().sendGameMessage("Sent player interact packet, id: " +packetId);
//			return;
//		}
	 if (packetId == INTERFACE_ON_OBJECT) {
		 

		 	int x = stream.readUnsignedShortLE128();
		 	boolean forceRun = stream.readUnsigned128Byte() == 1;
		 	int objectId = stream.readIntV1();
		 	int interfaceHash = stream.readInt();
		 	int itemId = stream.readUnsignedShortLE();
		 	int slot = stream.readUnsignedShort128();
		 	int y = stream.readUnsignedShortLE();
			
		 	int interfaceId = interfaceHash >> 16;
		 	int componentId = interfaceHash - (interfaceId << 16);
			
			System.out.println(String.format("%s, %s, %s, %s, %s, %s, %s", x, forceRun, objectId, interfaceHash, itemId, slot, y));
			System.out.println(String.format("%s, %s,", interfaceId, componentId));
			
			//3095, 0, 17010, 44498944, 11694, 0, 3503
//			0, 17010,
			
			if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
				return;
			long currentTime = Utility.currentTimeMillis();
			if (player.getMovement().getLockDelay() >= currentTime || player.getNextEmoteEnd() >= currentTime)
				return;
			final WorldTile tile = new WorldTile(x, y, player.getPlane());
			int regionId = tile.getRegionId();
			if (!player.getMapRegionsIds().contains(regionId))
				return;
			WorldObject mapObject = World.getRegion(regionId).getObject(objectId, tile);
			if (mapObject == null || mapObject.getId() != objectId)
				return;
			final WorldObject object = !player.isAtDynamicRegion() ? mapObject : new WorldObject(objectId, mapObject.getType(), mapObject.getRotation(), x, y, player.getPlane());
			final Item item = player.getInventory().getItem(slot);
			if (player.isDead() || CacheUtility.getInterfaceDefinitionsSize() <= interfaceId)
				return;
			if (player.getMovement().getLockDelay() > Utility.currentTimeMillis())
				return;
			if (!player.getInterfaceManager().containsInterface(interfaceId))
				return;
			if (item == null || item.getId() != itemId)
				return;
			player.getAttributes().stopAll(player, false); // false
			if (forceRun)
				player.setRun(forceRun);
			switch (interfaceId) {
			case Inventory.INVENTORY_INTERFACE: // inventory
				PluginManager.handle(new ItemOnObjectEvent(player, item, object, false));
				break;
			}
		} else if (packetId == PLAYER_OPTION_2_PACKET) {
			if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
				return;
			@SuppressWarnings("unused")
			boolean unknown = stream.readByte() == 1;
			int playerIndex = stream.readUnsignedShortLE128();
			Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.getMovement().getLockDelay() > Utility.currentTimeMillis())
				return;
			player.getAttributes().stopAll(player, false);
			player.getActionManager().setAction(new PlayerFollow(p2));
		} else if (packetId == PLAYER_OPTION_4_PACKET) {
			@SuppressWarnings("unused")
			boolean unknown = stream.readByte() == 1;
			int playerIndex = stream.readUnsignedShort(); //incorrect returns 32k
			Player p2 = World.getPlayers().get(playerIndex);
			if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()))
				return;
			if (player.getMovement().getLockDelay() > Utility.currentTimeMillis())
				return;
			player.getAttributes().stopAll(player, false);
			if (player.isCantTrade()) {
				player.getPackets().sendGameMessage("You are busy.");
				return;
			}
			if (p2.getInterfaceManager().containsScreenInter() || p2.isCantTrade()) {
				player.getPackets().sendGameMessage("The other player is busy.");
				return;
			}
			if (!p2.withinDistance(player, 14)) {
				player.getPackets().sendGameMessage("Unable to find target " + p2.getDisplayName());
				return;
			}

			if (p2.getTemporaryAttributtes().get("TradeTarget") == player) {
				p2.getTemporaryAttributtes().remove("TradeTarget");
				player.getTrade().openTrade(p2);
				p2.getTrade().openTrade(player);
				return;
			}
			player.getTemporaryAttributtes().put("TradeTarget", p2);
			player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() + " a request...");
			p2.getPackets().sendTradeRequestMessage(player);
		} else if (packetId == PLAYER_OPTION_1_PACKET) {
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
		} else if (packetId == ATTACK_NPC) {
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
		
		
		else if (packetId == GRAND_EXCHANGE_PACKET){
			int itemId = stream.readUnsignedShort();
			player.getPackets().sendGameMessage("ge Choose item: "+itemId);
			//player.getGeManager().chooseItem(itemId);
		}
		
		
		
		else if (packetId == INTERFACE_ON_PLAYER) {
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
		} else if (packetId == INTERFACE_ON_NPC) {
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
			
			System.out.println(String.format("%s, %s, %s, %s, %s,", interfaceHash, npcIndex, forceRun, itemId, interfaceSlot));
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
				if (item == null || ActivityHandler.execute(player, activity -> !activity.processItemOnNPC(player, npc, item)))
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
						player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
						if (ActivityHandler.execute(player, activity -> !activity.canAttack(player, npc)))
							return;
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
						player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
						if (ActivityHandler.execute(player, activity -> !activity.canAttack(player, npc)))
							return;
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
						player.getActionManager().setAction(new PlayerCombat(npc));
					}
					break;
				}
				break;
			}
			
				System.out.println("Spell:" + componentId);
		}
	 	if (packetId == OBJECT_CLICK1_PACKET)
	 		ObjectClickHandler.handleOption(player, stream, 1);
		else if (packetId == OBJECT_CLICK2_PACKET)
			ObjectClickHandler.handleOption(player, stream, 2);
		else if (packetId == OBJECT_CLICK3_PACKET)
			ObjectClickHandler.handleOption(player, stream, 3);
		else if (packetId == OBJECT_CLICK4_PACKET)
			ObjectClickHandler.handleOption(player, stream, 4);
		else if (packetId == OBJECT_CLICK5_PACKET)
			ObjectClickHandler.handleOption(player, stream, 5);
		else if (packetId == ITEM_TAKE_PACKET) {
			if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
				return;
			long currentTime = Utility.currentTimeMillis();
			if (player.getMovement().getLockDelay() > currentTime || player.getFreezeDelay() >= currentTime)
				return;

			final int id = stream.readShortLE128();
			boolean forceRun =  stream.readUnsignedByteC() == 1; 
			int y = stream.readUnsignedShort();
			int x = stream.readUnsignedShort128();
			
			System.out.println(x+", "+ y +", "+id +", "+forceRun);
			
			final WorldTile tile = new WorldTile(x, y, player.getPlane());
			final int regionId = tile.getRegionId();
			if (!player.getMapRegionsIds().contains(regionId)){
				return;
			}
			final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
			if (item == null)
				return;
			player.getAttributes().stopAll(player, false);
			if (forceRun)
				player.setRun(forceRun);
			player.setRouteEvent(new RouteEvent(item, new Runnable() {
				@Override
				public void run() {
					final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
					if (item == null)
						return;
					/*
					 * if (player.getRights() > 0 || player.isSupporter())
					 * player.getPackets().sendGameMessage("This item was dropped by [Username] "+item.getOwner().getUsername()+
					 * " [DiplayName] "+item.getOwner().getDisplayName());
					 */ player.setNextFaceWorldTile(tile);
					player.addWalkSteps(tile.getX(), tile.getY(), 1);
					FloorItem.removeGroundItem(player, item);
				}
			}, false));
		}
	 	NPCClickHandler.executeMobInteraction(player, stream, packetId == NPC_CLICK1_PACKET ? 1 :packetId ==  NPC_CLICK2_PACKET ? 2 :packetId ==  NPC_CLICK3_PACKET ? 3 : packetId == NPC_CLICK4_PACKET ? 4 : 5);
	}
}