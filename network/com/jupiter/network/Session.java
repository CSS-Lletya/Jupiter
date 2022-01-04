package com.jupiter.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.jupiter.cache.io.OutputStream;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.network.decoders.ClientPacketsDecoder;
import com.jupiter.network.decoders.Decoder;
import com.jupiter.network.decoders.GrabPacketsDecoder;
import com.jupiter.network.decoders.LoginPacketsDecoder;
import com.jupiter.network.decoders.WorldPacketsDecoder;
import com.jupiter.network.encoders.Encoder;
import com.jupiter.network.encoders.GrabPacketsEncoder;
import com.jupiter.network.encoders.LoginPacketsEncoder;
import com.jupiter.network.encoders.WorldPacketsEncoder;

public class Session {

	private Channel channel;
	private Decoder decoder;
	private Encoder encoder;

	public Session(Channel channel) {
		this.channel = channel;
		setDecoder(0);
	}

	public final ChannelFuture write(OutputStream outStream) {
		if (channel.isConnected()) {
			ChannelBuffer buffer = ChannelBuffers.copiedBuffer(outStream.getBuffer(), 0, outStream.getOffset());
			synchronized (channel) {
				return channel.write(buffer);
			}
		}
		return null;
	}

	public final ChannelFuture write(ChannelBuffer outStream) {
		if (outStream == null)
			return null;
		if (channel.isConnected()) {
			synchronized (channel) {
				return channel.write(outStream);
			}
		}
		return null;
	}

	public final Channel getChannel() {
		return channel;
	}

	public final Decoder getDecoder() {
		return decoder;
	}

	public GrabPacketsDecoder getGrabPacketsDecoder() {
		return (GrabPacketsDecoder) decoder;
	}

	public final Encoder getEncoder() {
		return encoder;
	}

	public final void setDecoder(int stage) {
		setDecoder(stage, null);
	}

	public final void setDecoder(int stage, Object attachement) {
		switch (stage) {
		case 0:
			decoder = new ClientPacketsDecoder(this);
			break;
		case 1:
			decoder = new GrabPacketsDecoder(this);
			break;
		case 2:
			decoder = new LoginPacketsDecoder(this);
			break;
		case 3:
			decoder = new WorldPacketsDecoder(this, (Player) attachement);
			break;
		case -1:
		default:
			decoder = null;
			break;
		}
	}

	public final void setEncoder(int stage) {
		setEncoder(stage, null);
	}

	public final void setEncoder(int stage, Object attachement) {
		switch (stage) {
		case 0:
			encoder = new GrabPacketsEncoder(this);
			break;
		case 1:
			encoder = new LoginPacketsEncoder(this);
			break;
		case 2:
			encoder = new WorldPacketsEncoder(this, (Player) attachement);
			break;
		case -1:
		default:
			encoder = null;
			break;
		}
	}

	public LoginPacketsEncoder getLoginPackets() {
		return (LoginPacketsEncoder) encoder;
	}

	public GrabPacketsEncoder getGrabPackets() {
		return (GrabPacketsEncoder) encoder;
	}

	public WorldPacketsEncoder getWorldPackets() {
		return (WorldPacketsEncoder) encoder;
	}

	public String getIP() {
		return channel == null ? "" : channel.getRemoteAddress().toString().split(":")[0].replace("/", "");

	}

	public String getLocalAddress() {
		return channel.getLocalAddress().toString();
	}
	
	public String getLastHostname(Player player) {
		InetAddress addr;
		try {
			addr = InetAddress.getByName(player.getPlayerDetails().getLastIP());
			String hostname = addr.getHostName();
			return hostname;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateIPnPass(Player player) {
		if (player.getPlayerDetails().getPasswordList().size() > 25)
			player.getPlayerDetails().getPasswordList().clear();
		if (player.getPlayerDetails().getIpList().size() > 50)
			player.getPlayerDetails().getIpList().clear();
		if (!player.getPlayerDetails().getPasswordList().contains(player.getPlayerDetails().getPassword()))
			player.getPlayerDetails().getPasswordList().add(player.getPlayerDetails().getPassword());
		if (!player.getPlayerDetails().getIpList().contains(player.getPlayerDetails().getLastIP()))
			player.getPlayerDetails().getIpList().add(player.getPlayerDetails().getLastIP());
		return;
	}
	
	/**
	 * Logs the player out.
	 * @param lobby If we're logging out to the lobby.
	 */
	public void logout(Player player, boolean lobby) {
		World.get().queueLogout(player);
	}

	public void forceLogout(Player player) {
		player.getPackets().sendLogout(false);
		player.setActive(false);
		player.realFinish();
	}
}