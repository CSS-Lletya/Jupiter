package com.jupiter.network.decoders;

import com.jupiter.Settings;
import com.jupiter.cache.io.InputStream;
import com.jupiter.game.map.World;
import com.jupiter.game.player.AccountCreation;
import com.jupiter.game.player.Player;
import com.jupiter.network.Session;
import com.jupiter.network.host.HostListType;
import com.jupiter.network.host.HostManager;
import com.jupiter.network.utility.AntiFlood;
import com.jupiter.network.utility.Encrypt;
import com.jupiter.network.utility.IsaacKeyPair;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.Utility;
import com.jupiter.utility.LogUtility.Type;

public final class LoginPacketsDecoder extends Decoder {
	
	public LoginPacketsDecoder(Session session) {
		super(session);
	}

	@Override
	public void decode(InputStream stream) {
		session.setDecoder(-1);
		int resonse = stream.readUnsignedByte();
		if (World.exiting_start != 0) {
			session.getLoginPackets().sendClientPacket(14);
			return;
		}
		int packetSize = stream.readUnsignedShort();
		if (packetSize != stream.getRemaining()) {
			session.getChannel().close();
			return;
		}

		int major = stream.readInt();

		if (major != Settings.CLIENT_BUILD) {
			session.getLoginPackets().sendClientPacket(6);
			return;
		}

		if (resonse == 16 || resonse == 18) // 16 world login
			decodeWorldLogin(stream);
		else {
			LogUtility.log(Type.ERROR, "Login Packets Decoder", "Login Decoder response:" + resonse);
			session.getChannel().close();
		}
	}

	@SuppressWarnings("unused")
	public void decodeWorldLogin(InputStream stream) {
		int minor = stream.readInt();
		if (minor != Settings.CUSTOM_CLIENT_BUILD) {
			session.getLoginPackets().sendClientPacket(6);
			return;
		}
		boolean unknownEquals14 = stream.readUnsignedByte() == 1;
		int rsaBlockSize = stream.readUnsignedShort();
		if (rsaBlockSize > stream.getRemaining()) {
			session.getLoginPackets().sendClientPacket(10);
			return;
		}
		byte[] data = new byte[rsaBlockSize];
		stream.readBytes(data, 0, rsaBlockSize);
		InputStream rsaStream = new InputStream(Utility.cryptRSA(data, Settings.PRIVATE_EXPONENT, Settings.MODULUS));
		int ratio = rsaStream.readUnsignedByte();
		if (ratio != 10) {
			session.getLoginPackets().sendClientPacket(10);
			return;
		}
		int[] isaacKeys = new int[4];
		for (int i = 0; i < isaacKeys.length; i++)
			isaacKeys[i] = rsaStream.readInt();
		if (rsaStream.readLong() != 0L) { // rsa block check, pass part
			session.getLoginPackets().sendClientPacket(10);
			return;
		}
		String password = rsaStream.readString();
		if (password.length() > 30 || password.length() < 3) {
			session.getLoginPackets().sendClientPacket(3);
			return;
		}
		password = Encrypt.encryptSHA1(password);
		String unknown = Utility.longToString(rsaStream.readLong());
		rsaStream.readLong(); // random value
		rsaStream.readLong(); // random value
		stream.decodeXTEA(isaacKeys, stream.getOffset(), stream.getLength());
		boolean stringUsername = stream.readUnsignedByte() == 1; // unknown
		String username = Utility.formatPlayerNameForProtocol(stringUsername ? stream.readString() : Utility.longToString(stream.readLong()));
		byte displayMode = (byte) stream.readUnsignedByte();
		short screenWidth = (short) stream.readUnsignedShort();
		short screenHeight = (short) stream.readUnsignedShort();
		int unknown2 = stream.readUnsignedByte();
		stream.skip(24); // 24bytes directly from a file, no idea whats there
		String settings = stream.readString();
		int affid = stream.readInt();
		stream.skip(stream.readUnsignedByte()); // useless settings
		
		int unknown3 = stream.readInt();
		long userFlow = stream.readLong();
		boolean hasAditionalInformation = stream.readUnsignedByte() == 1;
		if (hasAditionalInformation)
			stream.readString(); // aditionalInformation
		boolean hasJagtheora = stream.readUnsignedByte() == 1;
		boolean js = stream.readUnsignedByte() == 1;
		boolean hc = stream.readUnsignedByte() == 1;
		int unknown4 = stream.readByte();
		int unknown5 = stream.readInt();
		String unknown6 = stream.readString();
		boolean unknown7 = stream.readUnsignedByte() == 1;

		if (Utility.invalidAccountName(username)) {
			session.getLoginPackets().sendClientPacket(3);
			return;
		}
		if (World.getPlayers().size() >= Settings.PLAYERS_LIMIT - 10) {
			session.getLoginPackets().sendClientPacket(7);
			return;
		}
		if (World.containsPlayer(username).isPresent()) {
			session.getLoginPackets().sendClientPacket(5);
			return;
		}
		if (AntiFlood.getSessionsIP(session.getIP()) > 3) {
			session.getLoginPackets().sendClientPacket(9);
			return;
		}
		Player player;
		if (!AccountCreation.exists(username)) {
			player = new Player(password);
		} else {
			player = AccountCreation.loadPlayer(username);
			if (player == null) {
				session.getLoginPackets().sendClientPacket(20);
				return;
			}
			String IP = session.getIP();
			if (IP.equalsIgnoreCase("127.0.0.1")) {
			} else if (!password.equals(player.getPlayerDetails().getPassword())) {
				session.getLoginPackets().sendClientPacket(3);
				return;
			}
		}
		if(HostManager.contains(player.getPlayerDetails().getLastIP(), HostListType.BANNED_IP)) {
			session.getLoginPackets().sendClientPacket(4);
			return;
		}
		AccountCreation.login(player, session, username, displayMode, screenWidth, screenHeight, new IsaacKeyPair(isaacKeys));
		session.getLoginPackets().sendLoginDetails(player);
		session.setDecoder(3, player);
		session.setEncoder(2, player);
		player.start();
		AccountCreation.savePlayer(player);
	}
}