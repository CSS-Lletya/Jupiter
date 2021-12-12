package com.jupiter.utility;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;

import com.jupiter.utility.LogUtility.Type;

public final class MapArchiveKeys {

	private final static HashMap<Integer, int[]> keys = new HashMap<Integer, int[]>();
	private final static String PACKED_PATH = "data/map/archiveKeys/packed.mcx";

	public static final int[] getMapKeys(int regionId) {
		return keys.get(regionId);
	}

	public static void init() {
		if (new File(PACKED_PATH).exists())
			loadPackedKeys();
		else
			loadUnpackedKeys();
	}

	private static final void loadPackedKeys() {
		try {
			RandomAccessFile in = new RandomAccessFile(PACKED_PATH, "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			while (buffer.hasRemaining()) {
				int regionId = buffer.getShort() & 0xffff;
				int[] xteas = new int[4];
				for (int index = 0; index < 4; index++)
					xteas[index] = buffer.getInt();
				keys.put(regionId, xteas);
			}
			channel.close();
			in.close();
		} catch (Throwable e) {
			LogUtility.log(Type.ERROR, "Map Archive Keys", e.getMessage());
		}
	}

	public static final void loadUnpackedKeys() {
		LogUtility.log(Type.INFO, "Map Archive Keys", "Packing map containers xteas...");
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(PACKED_PATH));
			File unpacked = new File("data/map/archiveKeys/unpacked/");
			File[] xteasFiles = unpacked.listFiles();
			for (File region : xteasFiles) {
				String name = region.getName();
				if (!name.contains(".txt")) {
					region.delete();
					continue;
				}
				int regionId = Short.parseShort(name.replace(".txt", ""));
				if (regionId <= 0) {
					region.delete();
					continue;
				}
				BufferedReader in = new BufferedReader(new FileReader(region));
				out.writeShort(regionId);
				final int[] xteas = new int[4];
				for (int index = 0; index < 4; index++) {
					xteas[index] = Integer.parseInt(in.readLine());
					out.writeInt(xteas[index]);
				}
				keys.put(regionId, xteas);
				in.close();
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private MapArchiveKeys() {

	}

}
