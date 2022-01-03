package com.jupiter.network.decoders;

import com.jupiter.cache.io.InputStream;

public class LogicPacket {

	private int id;
	byte[] data;

	public LogicPacket(int packetId, int size, InputStream stream) {
		this.id = packetId;
		data = new byte[size];
		stream.getBytes(data, 0, size);
	}

	public int getId() {
		return id;
	}

	public byte[] getData() {
		return data;
	}

}
