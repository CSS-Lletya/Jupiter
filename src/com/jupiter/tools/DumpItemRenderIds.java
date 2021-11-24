package com.jupiter.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.jupiter.cache.Cache;
import com.jupiter.cache.loaders.ItemDefinitions;

public class DumpItemRenderIds {

	public static void main(String[] args) throws IOException {
		Cache.init();
		dumpRenders();
	}

	public static void dumpRenders() throws IOException {
		File file = new File("./renderids.txt");
		if (file.exists()) {
			file.delete();
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < 13247; i++) {
			ItemDefinitions def = ItemDefinitions.getItemDefinitions(i);
			if (def == null)
				continue;
			writer.write(i + "=" + def.getRenderAnimId());
			writer.newLine();
		}
		writer.flush();
		writer.close();
		System.out.println("Dumped all render ids.");
	}
}