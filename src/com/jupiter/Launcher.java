package com.jupiter;

import java.util.Arrays;

import com.jupiter.cache.Cache;
import com.jupiter.cache.loaders.ItemDefinitions;
import com.jupiter.cache.loaders.NPCDefinitions;
import com.jupiter.cache.loaders.ObjectDefinitions;
import com.jupiter.cores.CoresManager;
import com.jupiter.utils.Logger;
import com.jupiter.utils.Utils;
import com.rs.net.ServerChannelHandler;

import io.vavr.control.Try;

public final class Launcher {

	public static void main(String[] args) throws Exception {
		long currentTime = Utils.currentTimeMillis();

		GameLoader.getGameLoader().getBackgroundLoader().waitForPendingTasks().shutdown();

		Logger.log("Launcher",
				"Server took " + (Utils.currentTimeMillis() - currentTime) + " milli seconds to launch.");
		addCleanMemoryTask();
	}

	private static void addCleanMemoryTask() {
		CoresManager.schedule(() -> {
			cleanMemory(Runtime.getRuntime().freeMemory() < Settings.MIN_FREE_MEM_ALLOWED);
		}, 10);
	}

	/**
	 * The memory cleaning event contents. Here you can see what's being done specifically.
	 * @param force
	 */
	public static void cleanMemory(boolean force) {
		if (force) {
			ItemDefinitions.clearItemsDefinitions();
			NPCDefinitions.clearNPCDefinitions();
			ObjectDefinitions.clearObjectDefinitions();
		}
		Arrays.stream(Cache.STORE.getIndexes()).filter(index -> index != null).forEach(index -> index.resetCachedFiles());
		System.gc();
	}

	/**
	 * The shutdown hook fore the Network, then finally terminating the Application itself.
	 */
	public static void shutdown() {
		Try.runRunnable(() -> {
			ServerChannelHandler.shutdown();
			CoresManager.shutdown();
		}).andFinally(() -> System.exit(0));
	}
}