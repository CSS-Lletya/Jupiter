package com.jupiter.cores;

import java.util.concurrent.TimeUnit;

import com.jupiter.Settings;
import com.jupiter.game.map.World;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.LogUtility.Type;

import io.vavr.control.Try;
import lombok.SneakyThrows;

public final class WorldThread extends Thread {

	public static volatile long WORLD_CYCLE;

	protected WorldThread() {
		setPriority(Thread.MAX_PRIORITY);
		setName("World Thread");
		this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				LogUtility.log(Type.ERROR, "World Thread", ex.getMessage());
			}
		});
	}

	public static void init() {
		WORLD_CYCLE = System.currentTimeMillis() / 600L;
		CoresManager.getWorldExecutor().scheduleAtFixedRate(new WorldThread(), 0, Settings.WORLD_CYCLE_MS, TimeUnit.MILLISECONDS);
	}

	@Override
	@SneakyThrows(Throwable.class)
	public final void run() {
		WORLD_CYCLE++;
		Try.run(() -> {

			World.get().getTask().sequence();
			
			World.players().forEach(player -> player.processEntity());
			World.npcs().forEach(npc -> npc.processEntity());

			World.players().forEach(player -> {
				player.getPackets().sendLocalPlayersUpdate();
				player.getPackets().sendLocalNPCsUpdate();
			});
			
			World.entities().parallel().forEach(e -> e.resetMasks());

			World.get().dequeueLogout();
		}).onFailure(fail -> LogUtility.log(Type.ERROR, "World Thread", fail.getMessage()));
	}
}