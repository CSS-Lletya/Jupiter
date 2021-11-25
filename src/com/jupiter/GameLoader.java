package com.jupiter;

import java.util.concurrent.Executors;

import com.jupiter.cache.Cache;
import com.jupiter.cache.loaders.ItemsEquipIds;
import com.jupiter.combat.npc.combat.scripts.NPCCombatDispatcher;
import com.jupiter.combat.player.specials.WeaponSpecialDispatcher;
import com.jupiter.cores.BlockingExecutorService;
import com.jupiter.cores.CoresManager;
import com.jupiter.cores.WorldThread;
import com.jupiter.game.RegionBuilder;
import com.jupiter.game.World;
import com.jupiter.game.player.FriendChatsManager;
import com.jupiter.game.player.controlers.ControlerHandler;
import com.jupiter.plugins.CommandDispatcher;
import com.jupiter.plugins.InventoryDispatcher;
import com.jupiter.plugins.NPCDispatcher;
import com.jupiter.plugins.RSInterfaceDispatcher;
import com.jupiter.utils.Huffman;
import com.jupiter.utils.ItemBonuses;
import com.jupiter.utils.ItemExamines;
import com.jupiter.utils.Logger;
import com.jupiter.utils.MapArchiveKeys;
import com.jupiter.utils.MapAreas;
import com.jupiter.utils.NPCCombatDefinitionsL;
import com.rs.net.ServerChannelHandler;
import com.rs.net.host.HostListType;
import com.rs.net.host.HostManager;

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
		ServerChannelHandler.init();
		World.get().init();
		WorldThread.init();
		getBackgroundLoader().submit(() -> {
			ItemsEquipIds.init();
			Huffman.init();
		});
		getBackgroundLoader().submit(() -> {
			MapArchiveKeys.init();
			MapAreas.init();
			NPCCombatDefinitionsL.init();
		});
		getBackgroundLoader().submit(() -> {
//			NPCBonuses.init();
			ItemExamines.init();
			ItemBonuses.init();
//			MusicHints.init();
		});
		getBackgroundLoader().submit(() -> {
			ControlerHandler.init();
			FriendChatsManager.init();
			RegionBuilder.init();
		});
		getBackgroundLoader().submit(() -> {
			CommandDispatcher.load();
			RSInterfaceDispatcher.load();
			NPCDispatcher.load();
			NPCCombatDispatcher.load();
			InventoryDispatcher.load();
			WeaponSpecialDispatcher.load();
		});
		getBackgroundLoader().submit(() -> {
			HostManager.deserialize(HostListType.STARTER_RECEIVED);
			HostManager.deserialize(HostListType.BANNED_IP);
			HostManager.deserialize(HostListType.MUTED_IP);
		});
	}
	
	@Getter
	private static final GameLoader gameLoader = new GameLoader();
}