package com.jupiter.cores;

import com.jupiter.Settings;
import com.jupiter.game.World;
import com.jupiter.utils.Logger;
import com.jupiter.utils.Utils;

public final class WorldThread extends Thread {

	public WorldThread() {
		setPriority(Thread.MAX_PRIORITY);
		setName("World Thread");
	}

	@Override
	public final void run() {
		while (!CoresManager.shutdown) {
			long currentTime = Utils.currentTimeMillis();
			try {
				World.get().taskManager.sequence();
				World.get().dequeueLogout();
				World.players().forEach(p -> {
					if (currentTime - p.getPacketsDecoderPing() > Settings.MAX_PACKETS_DECODER_PING_DELAY
							&& p.getSession().getChannel().isOpen())
						p.getSession().getChannel().close();
					p.processEntity();
				});
				World.npcs().forEach(mob -> mob.processEntity());
			} catch (Throwable e) {
				Logger.handle(e);
			}
			try {
				World.players().forEach(p -> {
					p.getPackets().sendLocalPlayersUpdate();
					p.getPackets().sendLocalNPCsUpdate();
				});
			} catch (Throwable e) {
				Logger.handle(e);
			}
			World.entities().parallel().forEach((e) -> e.resetMasks());
			LAST_CYCLE_CTM = Utils.currentTimeMillis();
			long sleepTime = Settings.WORLD_CYCLE_TIME + currentTime - LAST_CYCLE_CTM;
			if (sleepTime <= 0)
				continue;
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static long LAST_CYCLE_CTM;
}