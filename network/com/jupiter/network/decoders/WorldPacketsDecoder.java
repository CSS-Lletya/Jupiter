package com.jupiter.network.decoders;

import java.util.stream.IntStream;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.Session;
import com.jupiter.network.packets.outgoing.OutgoingPacketDispatcher;

/**
 * Represents Decoder-based packet processing. Some packets are logic based,
 * some aren't so I simplified it by creating 2 separate systems to more easily
 * manage them. There's also some junk ones that I just removed. Anything missing
 * can reference Darkan (great example).
 * 
 * @author Dennis
 *
 */
public final class WorldPacketsDecoder extends Decoder {
	
	/**
	 * Represents the Player itself
	 */
	private transient Player player;

	/**
	 * Constructs a session with the player for decoding packets
	 * @param session
	 * @param player
	 */
	public WorldPacketsDecoder(Session session, Player player) {
		super(session);
		this.player = player;
	}

	/**
	 * Total number of packets
	 * (Below are packet identities, not going to bother describing each)
	 */
	private static final byte[] PACKET_SIZES = new byte[104];

	private final static int WALKING_PACKET = 33;
	private final static int MINIMAP_WALKING_PACKET = 42;
	
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
	
	private final static int OBJECT_CLICK1_PACKET = 75;
	private final static int OBJECT_CLICK2_PACKET = 93;
	private final static int OBJECT_CLICK3_PACKET = 38;
	private final static int OBJECT_CLICK4_PACKET = 32;
	private final static int OBJECT_CLICK5_PACKET = 48;
	
	private final static int ATTACK_NPC = 16;
	private final static int NPC_CLICK1_PACKET = 65;
	private final static int NPC_CLICK2_PACKET = 50;
	private final static int NPC_CLICK3_PACKET = 77;
	private final static int NPC_CLICK4_PACKET = 95;
	private final static int ITEM_TAKE_PACKET = 54;
	private final static int INTERFACE_ON_OBJECT = 98;
	private final static int INTERFACE_ON_PLAYER = 13;
	private final static int INTERFACE_ON_NPC = 41;
	public final static int WORLD_MAP_CLICK = 5;
	public final static int RECEIVE_PACKET_COUNT_PACKET = -1;
	
	@SuppressWarnings("unused")
	private static final int GRAND_EXCHANGE_PACKET = 17;
	@SuppressWarnings("unused")
	private final static int PUBLIC_QUICK_CHAT_PACKET = 64;
	@SuppressWarnings("unused")
	private final static int SEND_FRIEND_QUICK_CHAT_PACKET = 14;
	
	/**
	 * Loads the packet id & size on startup
	 */
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

	/**
	 * Decodes the packets
	 */
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
			}
			if (packetId < 0)
				System.out.println("PacketId " + packetId + " has . - expected size " + length);
			int startOffset = stream.getOffset();
			IntStream.of(LOGICAL_PACKETS).filter(size -> size == packetId).forEach(packet -> player.addLogicPacketToQueue(new LogicPacket(packetId, finalLength, stream)));
			OutgoingPacketDispatcher.execute(player, stream, packetId);
			stream.setOffset(startOffset + length);
		}
	}

	/**
	 * An immutable array of logic-based packets.
	 */
	private final int[] LOGICAL_PACKETS = {
			WALKING_PACKET,MINIMAP_WALKING_PACKET,ITEM_TAKE_PACKET,PLAYER_OPTION_1_PACKET,PLAYER_OPTION_2_PACKET,PLAYER_OPTION_3_PACKET,PLAYER_OPTION_4_PACKET,PLAYER_OPTION_9_PACKET,PLAYER_OPTION_10_PACKET, PLAYER_OPTION_9_PACKET,
			ATTACK_NPC,INTERFACE_ON_PLAYER,INTERFACE_ON_NPC,NPC_CLICK1_PACKET,NPC_CLICK2_PACKET,NPC_CLICK3_PACKET,NPC_CLICK4_PACKET, 
			OBJECT_CLICK1_PACKET,OBJECT_CLICK2_PACKET,OBJECT_CLICK3_PACKET,OBJECT_CLICK4_PACKET,OBJECT_CLICK5_PACKET,INTERFACE_ON_OBJECT
	};
}