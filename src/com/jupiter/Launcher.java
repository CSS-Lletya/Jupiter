package com.jupiter;

import java.util.concurrent.TimeUnit;

import com.alex.store.Index;
import com.jupiter.cache.Cache;
import com.jupiter.cache.loaders.ItemDefinitions;
import com.jupiter.cache.loaders.NPCDefinitions;
import com.jupiter.cache.loaders.ObjectDefinitions;
import com.jupiter.cores.CoresManager;
import com.jupiter.game.Region;
import com.jupiter.game.World;
import com.jupiter.game.player.AccountCreation;
import com.jupiter.json.impl.ObjectSpawnLoader;
import com.jupiter.utils.Logger;
import com.jupiter.utils.Utils;
import com.rs.net.ServerChannelHandler;

public final class Launcher {

	public static void main(String[] args) throws Exception {
		long currentTime = Utils.currentTimeMillis();

		GameLoader.get().getBackgroundLoader().waitForPendingTasks().shutdown();
		new ObjectSpawnLoader().initialize();

		Logger.log("Launcher",
				"Server took " + (Utils.currentTimeMillis() - currentTime) + " milli seconds to launch.");
		addAccountsSavingTask();
		addCleanMemoryTask();
	}

	private static void addCleanMemoryTask() {
		CoresManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					cleanMemory(Runtime.getRuntime().freeMemory() < Settings.MIN_FREE_MEM_ALLOWED);
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		}, 0, 10, TimeUnit.MINUTES);
	}

	private static void addAccountsSavingTask() {
		CoresManager.slowExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					World.players().forEach(p -> {
						AccountCreation.savePlayer(p);
					});
				} catch (Throwable e) {
					Logger.handle(e);
				}

			}
		}, 0, 10, TimeUnit.MINUTES);
	}

	public static void cleanMemory(boolean force) {
		if (force) {
			ItemDefinitions.clearItemsDefinitions();
			NPCDefinitions.clearNPCDefinitions();
			ObjectDefinitions.clearObjectDefinitions();
			for (Region region : World.getRegions().values())
				region.removeMapFromMemory();
		}
		for (Index index : Cache.STORE.getIndexes())
			index.resetCachedFiles();
		System.gc();
	}

	public static void shutdown() {
		try {
			ServerChannelHandler.shutdown();
			CoresManager.shutdown();
		} finally {
			System.exit(0);
		}
	}

	public static void restart() {
		ServerChannelHandler.shutdown();
		CoresManager.shutdown();
		System.gc();
		try {
			Runtime.getRuntime().exec(
					"java -server -Xms2048m -Xmx20000m -cp bin;/data/libs/netty-3.2.7.Final.jar;/data/libs/FileStore.jar Launcher false false true false");
			System.exit(0);
		} catch (Throwable e) {
			Logger.handle(e);
		}
	}
}