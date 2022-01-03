package com.jupiter;

import java.util.concurrent.Executors;

import com.jupiter.cache.Cache;
import com.jupiter.combat.npc.combat.scripts.NPCCombatDispatcher;
import com.jupiter.combat.player.specials.WeaponSpecialDispatcher;
import com.jupiter.cores.BlockingExecutorService;
import com.jupiter.cores.CoresManager;
import com.jupiter.cores.WorldThread;
import com.jupiter.game.map.RegionBuilder;
import com.jupiter.game.map.World;
import com.jupiter.game.player.content.FriendChatsManager;
import com.jupiter.network.ServerChannelHandler;
import com.jupiter.network.host.HostListType;
import com.jupiter.network.host.HostManager;
import com.jupiter.network.packets.outgoing.OutgoingPacketDispatcher;
import com.jupiter.network.utility.Huffman;
import com.jupiter.plugin.PluginManager;
import com.jupiter.plugins.commands.CommandDispatcher;
import com.jupiter.plugins.rsinterface.RSInterfaceDispatcher;
import com.jupiter.utility.ItemExamines;
import com.jupiter.utility.ItemWeights;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.LogUtility.Type;
import com.jupiter.utility.MapArchiveKeys;
import com.jupiter.utility.NPCBonuses;
import com.jupiter.utility.NPCCombatDefinitionsL;

import io.vavr.control.Try;
import lombok.Getter;

/**
 * Handles the game data being built for the server
 * (Server startups are sent here to be built on startup)
 * @author Dennis
 *
 */
public class GameLoader {

	/**
	 * Represents the Game Loader with the {@link #load()} method attached
	 */
	public GameLoader() {
		load();
	}

	/**
	 * Defines the executor service.
	 */
	@Getter
	private final BlockingExecutorService backgroundLoader = new BlockingExecutorService(Executors.newCachedThreadPool());

	/**
	 * Handles the data being loaded for startup
	 */
	public void load() {
		LogUtility.log(Type.INFO, "Game Loader", "Initializing Cache & Game Network...");
		Try.run(() -> Cache.init());
		CoresManager.startThreads();
		World.get().init();
		WorldThread.init();
		getBackgroundLoader().submit(() -> {
			Huffman.init();
			MapArchiveKeys.init();
			NPCCombatDefinitionsL.init();
			NPCBonuses.init();
			ItemExamines.init();
		});
		getBackgroundLoader().submit(() -> {
			FriendChatsManager.init();
			RegionBuilder.init();
			PluginManager.loadPlugins();
		});
		getBackgroundLoader().submit(() -> {
			CommandDispatcher.load();
			RSInterfaceDispatcher.load();
			NPCCombatDispatcher.load();
			WeaponSpecialDispatcher.load();
			OutgoingPacketDispatcher.load();
			ItemWeights.init();
		});
		getBackgroundLoader().submit(() -> {
			HostManager.deserialize(HostListType.STARTER_RECEIVED);
			HostManager.deserialize(HostListType.BANNED_IP);
			HostManager.deserialize(HostListType.MUTED_IP);
		});
		ServerChannelHandler.init();
	}
	
	/**
	 * Represents an instance of the {@link #GameLoader()} class
	 */
	@Getter
	private static final GameLoader gameLoader = new GameLoader();
}