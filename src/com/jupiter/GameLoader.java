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
import com.jupiter.net.ServerChannelHandler;
import com.jupiter.net.host.HostListType;
import com.jupiter.net.host.HostManager;
import com.jupiter.plugin.PluginManager;
import com.jupiter.plugins.commands.CommandDispatcher;
import com.jupiter.plugins.rsinterface.RSInterfaceDispatcher;
import com.jupiter.utils.Huffman;
import com.jupiter.utils.ItemExamines;
import com.jupiter.utils.Logger;
import com.jupiter.utils.MapArchiveKeys;
import com.jupiter.utils.NPCBonuses;
import com.jupiter.utils.NPCCombatDefinitionsL;

import io.vavr.control.Try;
import lombok.Getter;

public class GameLoader {

	public GameLoader() {
		load();
	}

	@Getter
	private final BlockingExecutorService backgroundLoader = new BlockingExecutorService(Executors.newCachedThreadPool());

	public void load() {
		Logger.log("Launcher", "Initializing Cache & Game Network...");
		Try.run(() -> Cache.init());
		CoresManager.startThreads();
		World.get().init();
		WorldThread.init();
		getBackgroundLoader().submit(() -> {
			Huffman.init();
			MapArchiveKeys.init();
			NPCCombatDefinitionsL.init();
		});
		getBackgroundLoader().submit(() -> {
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
		});
		getBackgroundLoader().submit(() -> {
			HostManager.deserialize(HostListType.STARTER_RECEIVED);
			HostManager.deserialize(HostListType.BANNED_IP);
			HostManager.deserialize(HostListType.MUTED_IP);
		});
		ServerChannelHandler.init();
	}
	
	@Getter
	private static final GameLoader gameLoader = new GameLoader();
}