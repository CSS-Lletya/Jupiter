package com.jupiter.game.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.jupiter.Launcher;
import com.jupiter.Settings;
import com.jupiter.combat.npc.NPC;
import com.jupiter.cores.CoresManager;
import com.jupiter.game.Entity;
import com.jupiter.game.EntityList;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.activity.impl.WildernessActivity;
import com.jupiter.game.task.Task;
import com.jupiter.game.task.TaskManager;
import com.jupiter.game.task.impl.DrainPrayerTask;
import com.jupiter.game.task.impl.DrainSkillsTask;
import com.jupiter.game.task.impl.PlayerOwnedObjectTask;
import com.jupiter.game.task.impl.RestoreHitpointsTask;
import com.jupiter.game.task.impl.RestoreRunEnergyTask;
import com.jupiter.game.task.impl.RestoreSkillsTask;
import com.jupiter.game.task.impl.RestoreSpecialTask;
import com.jupiter.network.encoders.other.Graphics;
import com.jupiter.network.utility.AntiFlood;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.Utility;
import com.jupiter.utility.LogUtility.Type;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public final class World {

	public static int exiting_delay;
	public static long exiting_start;

	private static final Predicate<Player> VALID_PLAYER = (p) -> p != null && p.isStarted() && !p.hasFinished() && p.isActive();
	private static final Predicate<NPC> VALID_NPC = (n) -> n != null && !n.hasFinished();

	public static Stream<Entity> entities() {
		return Stream.concat(players(), npcs());
	}

	public static Stream<Player> players() {
		return players.stream().filter(VALID_PLAYER);
	}

	public static Stream<NPC> npcs() {
		return npcs.stream().filter(VALID_NPC);
	}
	
	private static final EntityList<Player> players = new EntityList<Player>(Settings.PLAYERS_LIMIT);
	private static final EntityList<NPC> npcs = new EntityList<NPC>(Settings.NPCS_LIMIT);
	private static final Object2ObjectArrayMap<Integer, Region> regions = new Object2ObjectArrayMap<Integer, Region>();
	
	/**
	 * The queue of {@link Player}s waiting to be logged out.
	 */
	private final Queue<Player> logouts = new ConcurrentLinkedQueue<>();

	public final void init() {
		World.get().submit(new PlayerOwnedObjectTask());
		World.get().submit(new RestoreSpecialTask());
		World.get().submit(new DrainPrayerTask());
		World.get().submit(new RestoreRunEnergyTask());
		World.get().submit(new DrainSkillsTask());
		World.get().submit(new RestoreHitpointsTask());
		World.get().submit(new RestoreSkillsTask());
	}
	
	public static final Map<Integer, Region> getRegions() {
		return regions;
	}

	public static final Region getRegion(int id) {
		return getRegion(id, false);
	}

	public static final Region getRegion(int id, boolean load) {
		Region region = regions.get(id);
		if (region == null) {
			region = new Region(id);
			regions.put(id, region);
		}
		if (load)
			region.checkLoadMap();
		return region;
	}

	public static final void addPlayer(Player player) {
		players.add(player);
		AntiFlood.add(player.getSession().getIP());
	}

	public static void removePlayer(Player player) {
		players.remove(player);
		AntiFlood.remove(player.getSession().getIP());
	}

	public static final void addNPC(NPC npc) {
		npcs.add(npc);
	}

	public static final void removeNPC(NPC npc) {
		npcs.remove(npc);
	}
	
	public static final Optional<Player> containsPlayer(String username) {
		return players().filter(p -> p.getPlayerDetails().getUsername().equalsIgnoreCase(username)).findFirst();
	}

	public static final Player getPlayerByDisplayName(String username) {
		String formatedUsername = Utility.formatPlayerNameForDisplay(username);
		for (Player player : getPlayers()) {
			if (player == null)
				continue;
			if (player.getPlayerDetails().getUsername().equalsIgnoreCase(formatedUsername)
					|| player.getDisplayName().equalsIgnoreCase(formatedUsername))
				return player;
		}
		return null;
	}
	
	public static final EntityList<Player> getPlayers() {
		return players;
	}

	public static final EntityList<NPC> getNPCs() {
		return npcs;
	}

	public final void safeShutdown(final boolean restart, int delay) {
		if (exiting_start != 0)
			return;
		exiting_start = Utility.currentTimeMillis();
		exiting_delay = delay;
		players().forEach(p -> p.getPackets().sendSystemUpdate(delay));
		CoresManager.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					players().forEach(p -> p.realFinish());
					Launcher.shutdown();
				} catch (Throwable e) {
					LogUtility.log(Type.ERROR, "World", e.getMessage());
				}
			}
		}, delay);
	}

	public static final void sendGraphics(Entity creator, Graphics graphics, WorldTile tile) {
		if (creator == null) {
			players().filter(p -> !p.withinDistance(tile)).forEach(p -> p.getPackets().sendGraphics(graphics, tile));
		} else {
			for (int regionId : creator.getMapRegionsIds()) {
				List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
				if (playersIndexes == null)
					continue;
				for (Integer playerIndex : playersIndexes) {
					Player player = players.get(playerIndex);
					if (player == null || !player.isStarted() || player.hasFinished() || !player.withinDistance(tile))
						continue;
					player.getPackets().sendGraphics(graphics, tile);
				}
			}
		}
	}

	public static final void sendProjectile(Entity shooter, WorldTile startTile, WorldTile receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.isStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver)))
					continue;
				player.getPackets().sendProjectile(null, startTile, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 1);
			}
		}
	}

	public static final void sendProjectile(WorldTile shooter, Entity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : receiver.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.isStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver)))
					continue;
				player.getPackets().sendProjectile(null, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, 1);
			}
		}
	}

	public static final void sendProjectile(Entity shooter, WorldTile receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.isStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver)))
					continue;
				player.getPackets().sendProjectile(null, shooter, receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, shooter.getSize());
			}
		}
	}

	public static final void sendProjectile(Entity shooter, Entity receiver, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset) {
		for (int regionId : shooter.getMapRegionsIds()) {
			List<Integer> playersIndexes = getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player player = players.get(playerIndex);
				if (player == null || !player.isStarted() || player.hasFinished() || (!player.withinDistance(shooter) && !player.withinDistance(receiver)))
					continue;
				int size = shooter.getSize();
				player.getPackets().sendProjectile(receiver, new WorldTile(shooter.getCoordFaceX(size), shooter.getCoordFaceY(size), shooter.getPlane()), receiver, gfxId, startHeight, endHeight, speed, delay, curve, startDistanceOffset, size);
			}
		}
	}

	public static final boolean isMultiArea(WorldTile tile) {
		int destX = tile.getX();
		int destY = tile.getY();
		return (destX >= 3462 && destX <= 3511 && destY >= 9481 && destY <= 9521 && tile.getPlane() == 0) // kalphite queen lair
				|| (destX >= 4540 && destX <= 4799 && destY >= 5052 && destY <= 5183 && tile.getPlane() == 0) // thzaar city
				|| (destX >= 1721 && destX <= 1791 && destY >= 5123 && destY <= 5249) // mole
				|| (destX >= 3029 && destX <= 3374 && destY >= 3759 && destY <= 3903)// wild
				|| (destX >= 2250 && destX <= 2280 && destY >= 4670 && destY <= 4720) || (destX >= 3198 && destX <= 3380 && destY >= 3904 && destY <= 3970) || (destX >= 3191 && destX <= 3326 && destY >= 3510 && destY <= 3759) || (destX >= 2987 && destX <= 3006 && destY >= 3912 && destY <= 3937) || (destX >= 2245 && destX <= 2295 && destY >= 4675 && destY <= 4720) || (destX >= 2450 && destX <= 3520 && destY >= 9450 && destY <= 9550) || (destX >= 3006 && destX <= 3071 && destY >= 3602 && destY <= 3710) || (destX >= 3134 && destX <= 3192 && destY >= 3519 && destY <= 3646) || (destX >= 2815 && destX <= 2966 && destY >= 5240 && destY <= 5375)// wild
				|| (destX >= 2840 && destX <= 2950 && destY >= 5190 && destY <= 5230) // godwars
				|| (destX >= 3547 && destX <= 3555 && destY >= 9690 && destY <= 9699) // zaros  godwars
				|| (destX >= 2970 && destX <= 3000 && destY >= 4365 && destY <= 4400)// corp
				|| (destX >= 3195 && destX <= 3327 && destY >= 3520 && destY <= 3970 || (destX >= 2376 && 5127 >= destY && destX <= 2422 && 5168 <= destY)) || (destX >= 2374 && destY >= 5129 && destX <= 2424 && destY <= 5168) // pits
				|| (destX >= 2622 && destY >= 5696 && destX <= 2573 && destY <= 5752) // torms
				|| (destX >= 2368 && destY >= 3072 && destX <= 2431 && destY <= 3135) // castlewars
				// out
				|| (destX >= 2365 && destY >= 9470 && destX <= 2436 && destY <= 9532) // castlewars
				|| (destX >= 2948 && destY >= 5537 && destX <= 3071 && destY <= 5631) // Risk
				// ffa.
				|| (destX >= 2756 && destY >= 5537 && destX <= 2879 && destY <= 5631) // Safe ffa

				|| (tile.getX() >= 3011 && tile.getX() <= 3132 && tile.getY() >= 10052 && tile.getY() <= 10175 && (tile.getY() >= 10066 || tile.getX() >= 3094)) // fortihrny dungeon
		;
		// in

		// multi
	}

	public static final boolean isPvpArea(WorldTile tile) {
		return WildernessActivity.isAtWild(tile);
	}

	public static void sendWorldMessage(String message) {
		players().forEach(player -> player.getPackets().sendGameMessage(message));
	}

	public static final void sendProjectile(WorldObject object, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startOffset) {
		for (Player pl : players) {
			if (pl == null || !pl.withinDistance(object, 20))
				continue;
			pl.getPackets().sendProjectile(null, startTile, endTile, gfxId, startHeight, endHeight, speed, delay, curve, startOffset, 1);
		}
	}
	
	/**
	 * An implementation of the singleton pattern to prevent indirect
	 * instantiation of this class file.
	 */
	private static final World singleton = new World();
	
	/**
	 * Returns the singleton pattern implementation.
	 * @return The returned implementation.
	 */
	public static World get() {
		return singleton;
	}
	
	/**
	 * The manager for the queue of game tasks.
	 */
	public final TaskManager taskManager = new TaskManager();
	
	/**
	 * Submits {@code t} to the backing {@link TaskManager}.
	 * @param t the task to submit to the queue.
	 */
	public void submit(Task t) {
		taskManager.submit(t);
	}

	/**
	 * Gets the manager for the queue of game tasks.
	 * @return the queue of tasks.
	 */
	public TaskManager getTask() {
		return taskManager;
	}
	
	/**
	 * Queues {@code player} to be logged out on the next server sequence.
	 * @param player the player to log out.
	 */
	public void queueLogout(Player player) {
		if (!player.isActive())
			return;
		long currentTime = Utility.currentTimeMillis();
		if (player.getAttackedByDelay() + 10000 > currentTime) {
			player.getPackets().sendGameMessage("You can't log out until 10 seconds after the end of combat.");
			return;
		}
		if (player.getNextEmoteEnd() >= currentTime) {
			player.getPackets().sendGameMessage("You can't log out while performing an emote.");
			return;
		}
		if (player.getMovement().getLockDelay() >= currentTime) {
			player.getPackets().sendGameMessage("You can't log out while performing an action.");
			return;
		}
		player.setRun(false);
		logouts.add(player);
	}
	
	/**
	 * Dequeue the logged out demands.
	 */
	public void dequeueLogout() {
		if(!logouts.isEmpty()) {
			for(int i = 0; i < Settings.LOGOUT_THRESHOLD; i++) {
				Player player = logouts.poll();
				if(player == null) {
					continue;
				}
				player.getPackets().sendLogout(false);
				logouts.offer(player);
			}
		}
	}

	public static List<Player> getPlayersInRegionRange(int regionId) {
		List<Player> players = new ArrayList<>();
		for (Player player : getPlayers()) {
			if (player == null)
				continue;
			if (player.getMapRegionsIds().contains(regionId))
				players.add(player);
		}
		return players;
	}
}