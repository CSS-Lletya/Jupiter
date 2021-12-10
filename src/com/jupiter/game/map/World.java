package com.jupiter.game.map;

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
import com.jupiter.game.item.FloorItem;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.game.player.controlers.Wilderness;
import com.jupiter.game.route.ClipFlag;
import com.jupiter.game.route.ClipType;
import com.jupiter.game.route.Direction;
import com.jupiter.game.route.Flags;
import com.jupiter.game.task.Task;
import com.jupiter.game.task.TaskManager;
import com.jupiter.game.task.impl.DrainPrayerTask;
import com.jupiter.game.task.impl.DrainSkillsTask;
import com.jupiter.game.task.impl.PlayerOwnedObjectTask;
import com.jupiter.game.task.impl.RestoreHitpointsTask;
import com.jupiter.game.task.impl.RestoreRunEnergyTask;
import com.jupiter.game.task.impl.RestoreSkillsTask;
import com.jupiter.game.task.impl.RestoreSpecialTask;
import com.jupiter.net.encoders.other.Graphics;
import com.jupiter.utils.AntiFlood;
import com.jupiter.utils.Logger;
import com.jupiter.utils.Utils;

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
	
	/*
	 * checks clip
	 */
	public static boolean canMoveNPC(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++)
			for (int tileY = y; tileY < y + size; tileY++)
				if (getMask(plane, tileX, tileY) != 0)
					return false;
		return true;
	}

	/*
	 * checks clip
	 */
	public static boolean isNotCliped(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++)
			for (int tileY = y; tileY < y + size; tileY++)
				if ((getMask(plane, tileX, tileY) & 2097152) != 0)
					return false;
		return true;
	}

	public static int getMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return -1;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getMask(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static void setMask(int plane, int x, int y, int mask) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		region.setMask(tile.getPlane(), baseLocalX, baseLocalY, mask);
	}

	public static int getRotation(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return 0;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getRotation(tile.getPlane(), baseLocalX, baseLocalY);
	}

	private static int getClipedOnlyMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = getRegion(regionId);
		if (region == null)
			return -1;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getMaskClipedOnly(tile.getPlane(), baseLocalX, baseLocalY);
	}

	private static int getClipFlagsProj(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		Region region = getRegion(tile.getRegionId());
		if (region == null)
			return -1;
		return region.getClipFlagsProj(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
	}
	
	public static final boolean checkProjectileStep(int plane, int x, int y, int dir, int size) {
		int xOffset = Utils.DIRECTION_DELTA_X[dir];
		int yOffset = Utils.DIRECTION_DELTA_Y[dir];
		if (size == 1) {
			int mask = getClipedOnlyMask(plane, x + Utils.DIRECTION_DELTA_X[dir], y + Utils.DIRECTION_DELTA_Y[dir]);
			if (xOffset == -1 && yOffset == 0)
				return (mask & 0x42240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (mask & 0x60240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (mask & 0x40a40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (mask & 0x48240000) == 0;
			if (xOffset == -1 && yOffset == -1) {
				return (mask & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == 1 && yOffset == -1) {
				return (mask & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x40a40000) == 0;
			}
			if (xOffset == -1 && yOffset == 1) {
				return (mask & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x - 1, y) & 0x42240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
			}
			if (xOffset == 1 && yOffset == 1) {
				return (mask & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y) & 0x60240000) == 0 && (getClipedOnlyMask(plane, x, y + 1) & 0x48240000) == 0;
			}
		} else if (size == 2) {
			if (xOffset == -1 && yOffset == 0)
				return (getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0;
			if (xOffset == 1 && yOffset == 0)
				return (getClipedOnlyMask(plane, x + 2, y) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 1) & 0x78240000) == 0;
			if (xOffset == 0 && yOffset == -1)
				return (getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x + 1, y - 1) & 0x60e40000) == 0;
			if (xOffset == 0 && yOffset == 1)
				return (getClipedOnlyMask(plane, x, y + 2) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 2) & 0x78240000) == 0;
			if (xOffset == -1 && yOffset == -1)
				return (getClipedOnlyMask(plane, x - 1, y) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) == 0 && (getClipedOnlyMask(plane, x, y - 1) & 0x63e40000) == 0;
			if (xOffset == 1 && yOffset == -1)
				return (getClipedOnlyMask(plane, x + 1, y - 1) & 0x63e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y - 1) & 0x60e40000) == 0 && (getClipedOnlyMask(plane, x + 2, y) & 0x78e40000) == 0;
			if (xOffset == -1 && yOffset == 1)
				return (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4fa40000) == 0 && (getClipedOnlyMask(plane, x - 1, y + 1) & 0x4e240000) == 0 && (getClipedOnlyMask(plane, x, y + 2) & 0x7e240000) == 0;
			if (xOffset == 1 && yOffset == 1)
				return (getClipedOnlyMask(plane, x + 1, y + 2) & 0x7e240000) == 0 && (getClipedOnlyMask(plane, x + 2, y + 2) & 0x78240000) == 0 && (getClipedOnlyMask(plane, x + 1, y + 1) & 0x78e40000) == 0;
		} else {
			if (xOffset == -1 && yOffset == 0) {
				if ((getClipedOnlyMask(plane, x - 1, y) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x - 1, -1 + (y + size)) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 0) {
				if ((getClipedOnlyMask(plane, x + size, y) & 0x60e40000) != 0 || (getClipedOnlyMask(plane, x + size, y - (-size + 1)) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x, y - 1) & 0x43a40000) != 0 || (getClipedOnlyMask(plane, x + size - 1, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 0 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x, y + size) & 0x4e240000) != 0 || (getClipedOnlyMask(plane, x + (size - 1), y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x - 1, y - 1) & 0x43a40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x - 1, y + (-1 + sizeOffset)) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, sizeOffset - 1 + x, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == -1) {
				if ((getClipedOnlyMask(plane, x + size, y - 1) & 0x60e40000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + size, sizeOffset + (-1 + y)) & 0x78e40000) != 0 || (getClipedOnlyMask(plane, x + sizeOffset, y - 1) & 0x63e40000) != 0)
						return false;
			} else if (xOffset == -1 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x - 1, y + size) & 0x4e240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x - 1, y + sizeOffset) & 0x4fa40000) != 0 || (getClipedOnlyMask(plane, -1 + (x + sizeOffset), y + size) & 0x7e240000) != 0)
						return false;
			} else if (xOffset == 1 && yOffset == 1) {
				if ((getClipedOnlyMask(plane, x + size, y + size) & 0x78240000) != 0)
					return false;
				for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
					if ((getClipedOnlyMask(plane, x + sizeOffset, y + size) & 0x7e240000) != 0 || (getClipedOnlyMask(plane, x + size, y + sizeOffset) & 0x78e40000) != 0)
						return false;
			}
		}
		return true;
	}

	public static boolean hasLineOfSight(WorldTile t1, WorldTile t2) {
		if (t1.getPlane() != t2.getPlane())
			return false;
		int plane = t1.getPlane();
		
		int x1 = t1.getX();
		int x2 = t2.getX();
		int y1 = t1.getY();
		int y2 = t2.getY();

		int dx = x2 - x1;
		int dxAbs = Math.abs(dx);
		int dy = y2 - y1;
		int dyAbs = Math.abs(dy);

		if (dxAbs > dyAbs) {
			int xTile = x1;
			int y = (y1 << 16) + 0x8000;
			int slope = (int) ((double) (dy << 16) / dxAbs); //Runescript no floating point values rofl

			int xInc;
			int xMask;
			if (dx > 0) {
				xInc = 1;
				xMask = ClipFlag.or(ClipFlag.BP_W, ClipFlag.BP_FULL);
			} else {
				xInc = -1;
				xMask = ClipFlag.or(ClipFlag.BP_E, ClipFlag.BP_FULL);
			}
			int yMask;
			if (dy < 0) {
				y -= 1;
				yMask = ClipFlag.or(ClipFlag.BP_N, ClipFlag.BP_FULL);
			} else {
				yMask = ClipFlag.or(ClipFlag.BP_S, ClipFlag.BP_FULL);
			}

			while (xTile != x2) {
				xTile += xInc;
				int yTile = y >>> 16;
				if ((getClipFlagsProj(plane, xTile, yTile) & xMask) != 0) {
					return false;
				}
				y += slope;
				int newYTile = y >>> 16;
				if (newYTile != yTile && (getClipFlagsProj(plane, xTile, newYTile) & yMask) != 0) {
					return false;
				}
			}
		} else {
			int yTile = y1;
			int x = (x1 << 16) + 0x8000;
			int slope = (int) ((double) (dx << 16) / dyAbs);

			int yInc;
			int yMask;
			if (dy > 0) {
				yInc = 1;
				yMask = ClipFlag.or(ClipFlag.BP_S, ClipFlag.BP_FULL);
			} else {
				yInc = -1;
				yMask = ClipFlag.or(ClipFlag.BP_N, ClipFlag.BP_FULL);
			}

			int xMask;
			if (dx < 0) {
				x -= 1;
				xMask = ClipFlag.or(ClipFlag.BP_E, ClipFlag.BP_FULL);
			} else {
				xMask = ClipFlag.or(ClipFlag.BP_W, ClipFlag.BP_FULL);
			}
			if (dxAbs == dyAbs) {
				//Runetek 5 diagonal check
				int xInc = (dx > 0 ? 1 : -1);
				int xTile = x1;
				while (yTile != y2) {
					if (((getClipFlagsProj(plane, xTile + xInc, yTile) & xMask) != 0 || (getClipFlagsProj(plane, xTile + xInc, yTile + yInc) & yMask) != 0) && 
						((getClipFlagsProj(plane, xTile, yTile + yInc) & yMask) != 0 || (getClipFlagsProj(plane, xTile + xInc, yTile + yInc) & xMask) != 0)) {
						return false;
					}
					xTile += xInc;
					yTile += yInc;
				}
			} else {
				while (yTile != y2) {
					yTile += yInc;
					int xTile = x >>> 16;
					if ((getClipFlagsProj(plane, xTile, yTile) & yMask) != 0) {
						return false;
					}
					x += slope;
					int newXTile = x >>> 16;
					if (newXTile != xTile && (getClipFlagsProj(plane, newXTile, yTile) & xMask) != 0) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static boolean checkMeleeStep(WorldTile from, WorldTile to) {
		if (to instanceof Entity && from instanceof Entity) {
			WorldTile closestFrom = from;
			WorldTile closestTo = to;
			int sizeFrom = ((Entity)from).getSize();
			int sizeTo = ((Entity)to).getSize();
			double shortest = 1000.0;
			for (int x1 = 0; x1 < sizeFrom; x1++) {
				for (int y1 = 0; y1 < sizeFrom; y1++) {
					for (int x2 = 0; x2 < sizeTo; x2++) {
						for (int y2 = 0; y2 < sizeTo; y2++) {
							double dist = Utils.getDistance(from.transform(x1, y1), to.transform(x2, y2));
							if (dist < shortest) {
								closestFrom = from.transform(x1, y1);
								closestTo = to.transform(x2, y2);
								shortest = dist;
							}
						}
					}
				}
			}
			from = closestFrom;
			to = closestTo;
		}
		if (from.matches(to))
			return true;
		switch(Direction.forDelta(to.getX()-from.getX(), to.getY()-from.getY())) {
		case NORTHEAST:
		case NORTHWEST:
		case SOUTHEAST:
		case SOUTHWEST:
			return false;
		default:
			break;
		
		}
		return checkWalkStep(from, to, 1);
	}
	public static final boolean checkWalkStep(WorldTile from, WorldTile to, int size) {
		return checkWalkStep(from, to, size, ClipType.NORMAL);
	}

	public static final boolean checkWalkStep(WorldTile from, WorldTile to, int size, ClipType type) {
		Direction dir = Direction.forDelta(to.getX() - from.getX(), to.getY() - from.getY());
		return checkWalkStep(from.getPlane(), from.getX(), from.getY(), dir, size, type);
	}
	
	public static final boolean checkWalkStep(WorldTile tile, Direction dir, int size) {
		return checkWalkStep(tile.getPlane(), tile.getX(), tile.getY(), dir.getDx(), dir.getDy(), size);
	}
	
	public static final boolean checkWalkStep(int plane, int x, int y, Direction dir, int size) {
		return checkWalkStep(plane, x, y, dir, size, ClipType.NORMAL);
	}

	public static final boolean checkWalkStep(int plane, int x, int y, Direction dir, int size, ClipType type) {
		return checkWalkStep(plane, x, y, dir.getDx(), dir.getDy(), size, type);
	}
	
	public static final boolean checkWalkStep(int plane, int x, int y, int xOffset, int yOffset, int size) {
		return checkWalkStep(plane, x, y, xOffset, yOffset, size, ClipType.NORMAL);
	}

	public static final boolean checkWalkStep(int plane, int x, int y, int xOffset, int yOffset, int size, ClipType type) {
		switch(type) {
		case FLYING:
			if (size == 1) {
				int flags = getClipFlagsProj(plane, x + xOffset, y + yOffset);
				if (xOffset == -1 && yOffset == 0)
					return !ClipFlag.flagged(flags, ClipFlag.BP_FULL, ClipFlag.BP_E);
				if (xOffset == 1 && yOffset == 0)
					return !ClipFlag.flagged(flags, ClipFlag.BP_FULL, ClipFlag.BP_W);
				if (xOffset == 0 && yOffset == -1)
					return !ClipFlag.flagged(flags, ClipFlag.BP_FULL, ClipFlag.BP_N);
				if (xOffset == 0 && yOffset == 1)
					return !ClipFlag.flagged(flags, ClipFlag.BP_FULL, ClipFlag.BP_S);
				if (xOffset == -1 && yOffset == -1)
					return !ClipFlag.flagged(flags, ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_NE) && 
							!ClipFlag.flagged(getClipFlagsProj(plane, x - 1, y), ClipFlag.BP_FULL, ClipFlag.BP_E) && 
							!ClipFlag.flagged(getClipFlagsProj(plane, x, y - 1), ClipFlag.BP_FULL, ClipFlag.BP_N);
				if (xOffset == 1 && yOffset == -1)
					return !ClipFlag.flagged(flags, ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_W, ClipFlag.BP_NW) && 
							!ClipFlag.flagged(getClipFlagsProj(plane, x + 1, y), ClipFlag.BP_FULL, ClipFlag.BP_W) && 
							!ClipFlag.flagged(getClipFlagsProj(plane, x, y - 1), ClipFlag.BP_FULL, ClipFlag.BP_N);
				if (xOffset == -1 && yOffset == 1)
					return !ClipFlag.flagged(flags, ClipFlag.BP_FULL, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_SE) && 
							!ClipFlag.flagged(getClipFlagsProj(plane, x - 1, y), ClipFlag.BP_FULL, ClipFlag.BP_E) && 
							!ClipFlag.flagged(getClipFlagsProj(plane, x, y + 1), ClipFlag.BP_FULL, ClipFlag.BP_S);
				if (xOffset == 1 && yOffset == 1)
					return !ClipFlag.flagged(flags, ClipFlag.BP_FULL, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_SW) && 
							!ClipFlag.flagged(getClipFlagsProj(plane, x + 1, y), ClipFlag.BP_FULL, ClipFlag.BP_W) && 
							!ClipFlag.flagged(getClipFlagsProj(plane, x, y + 1), ClipFlag.BP_FULL, ClipFlag.BP_S);
			} else {
				if (xOffset == -1 && yOffset == 0) {
					if (ClipFlag.flagged(getClipFlagsProj(plane, x - 1, y), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_NE) || 
						ClipFlag.flagged(getClipFlagsProj(plane, x - 1, -1 + (y + size)), ClipFlag.BP_FULL, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_SE))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlagsProj(plane, x - 1, y + sizeOffset), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_NE, ClipFlag.BP_SE))
							return false;
				} else if (xOffset == 1 && yOffset == 0) {
					if (ClipFlag.flagged(getClipFlagsProj(plane, x + size, y), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_W, ClipFlag.BP_NW) || 
						ClipFlag.flagged(getClipFlagsProj(plane, x + size, y - (-size + 1)), ClipFlag.BP_FULL, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_SW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlagsProj(plane, x + size, y + sizeOffset), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_NW, ClipFlag.BP_SW))
							return false;
				} else if (xOffset == 0 && yOffset == -1) {
					if (ClipFlag.flagged(getClipFlagsProj(plane, x, y - 1), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_NE) || 
						ClipFlag.flagged(getClipFlagsProj(plane, x + size - 1, y - 1), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_W, ClipFlag.BP_NW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlagsProj(plane, x + sizeOffset, y - 1), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_W, ClipFlag.BP_NW, ClipFlag.BP_NE))
							return false;
				} else if (xOffset == 0 && yOffset == 1) {
					if (ClipFlag.flagged(getClipFlagsProj(plane, x, y + size), ClipFlag.BP_FULL, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_SE) || 
						ClipFlag.flagged(getClipFlagsProj(plane, x + (size - 1), y + size), ClipFlag.BP_FULL, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_SW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlagsProj(plane, x + sizeOffset, y + size), ClipFlag.BP_FULL, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_SE, ClipFlag.BP_SW))
							return false;
				} else if (xOffset == -1 && yOffset == -1) {
					if (ClipFlag.flagged(getClipFlagsProj(plane, x - 1, y - 1), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_NE))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlagsProj(plane, x - 1, y + (-1 + sizeOffset)), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_NE, ClipFlag.BP_SE) || 
							ClipFlag.flagged(getClipFlagsProj(plane, sizeOffset - 1 + x, y - 1), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_W, ClipFlag.BP_NW, ClipFlag.BP_NE))
							return false;
				} else if (xOffset == 1 && yOffset == -1) {
					if (ClipFlag.flagged(getClipFlagsProj(plane, x + size, y - 1), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_W, ClipFlag.BP_NW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlagsProj(plane, x + size, sizeOffset + (-1 + y)), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_NW, ClipFlag.BP_SW) || 
							ClipFlag.flagged(getClipFlagsProj(plane, x + sizeOffset, y - 1), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_W, ClipFlag.BP_NW, ClipFlag.BP_NE))
							return false;
				} else if (xOffset == -1 && yOffset == 1) {
					if (ClipFlag.flagged(getClipFlagsProj(plane, x - 1, y + size), ClipFlag.BP_FULL, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_SE))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlagsProj(plane, x - 1, y + sizeOffset), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_NE, ClipFlag.BP_SE) || 
							ClipFlag.flagged(getClipFlagsProj(plane, -1 + (x + sizeOffset), y + size), ClipFlag.BP_FULL, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_SE, ClipFlag.BP_SW))
							return false;
				} else if (xOffset == 1 && yOffset == 1) {
					if (ClipFlag.flagged(getClipFlagsProj(plane, x + size, y + size), ClipFlag.BP_FULL, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_SW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlagsProj(plane, x + sizeOffset, y + size), ClipFlag.BP_FULL, ClipFlag.BP_E, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_SE, ClipFlag.BP_SW) || 
							ClipFlag.flagged(getClipFlagsProj(plane, x + size, y + sizeOffset), ClipFlag.BP_FULL, ClipFlag.BP_N, ClipFlag.BP_S, ClipFlag.BP_W, ClipFlag.BP_NW, ClipFlag.BP_SW))
							return false;
				}
			}
			return true;
		case WATER:
			if (size == 1) {
				int flags = getClipFlags(plane, x + xOffset, y + yOffset);
				if (xOffset == -1 && yOffset == 0)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E);
				if (xOffset == 1 && yOffset == 0)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_W);
				if (xOffset == 0 && yOffset == -1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N);
				if (xOffset == 0 && yOffset == 1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S);
				if (xOffset == -1 && yOffset == -1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_NE) && 
							!ClipFlag.flagged(getClipFlags(plane, x - 1, y), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E) && 
							!ClipFlag.flagged(getClipFlags(plane, x, y - 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N);
				if (xOffset == 1 && yOffset == -1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_W, ClipFlag.BW_NW) && 
							!ClipFlag.flagged(getClipFlags(plane, x + 1, y), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_W) && 
							!ClipFlag.flagged(getClipFlags(plane, x, y - 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N);
				if (xOffset == -1 && yOffset == 1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_SE) && 
							!ClipFlag.flagged(getClipFlags(plane, x - 1, y), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E) && 
							!ClipFlag.flagged(getClipFlags(plane, x, y + 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S);
				if (xOffset == 1 && yOffset == 1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SW) && 
							!ClipFlag.flagged(getClipFlags(plane, x + 1, y), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_W) && 
							!ClipFlag.flagged(getClipFlags(plane, x, y + 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S);
			} else {
				if (xOffset == -1 && yOffset == 0) {
					if (ClipFlag.flagged(getClipFlags(plane, x - 1, y), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_NE) 
							|| ClipFlag.flagged(getClipFlags(plane, x - 1, -1 + (y + size)), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_SE))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x - 1, y + sizeOffset), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_NE, ClipFlag.BW_SE))
							return false;
				} else if (xOffset == 1 && yOffset == 0) {
					if (ClipFlag.flagged(getClipFlags(plane, x + size, y), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_W, ClipFlag.BW_NW) 
							|| ClipFlag.flagged(getClipFlags(plane, x + size, y - (-size + 1)), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + size, y + sizeOffset), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_SW))
							return false;
				} else if (xOffset == 0 && yOffset == -1) {
					if (ClipFlag.flagged(getClipFlags(plane, x, y - 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_NE) 
							|| ClipFlag.flagged(getClipFlags(plane, x + size - 1, y - 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_W, ClipFlag.BW_NW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + sizeOffset, y - 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_NE))
							return false;
				} else if (xOffset == 0 && yOffset == 1) {
					if (ClipFlag.flagged(getClipFlags(plane, x, y + size), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_SE) 
							|| ClipFlag.flagged(getClipFlags(plane, x + (size - 1), y + size), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + sizeOffset, y + size), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SE, ClipFlag.BW_SW))
							return false;
				} else if (xOffset == -1 && yOffset == -1) {
					if (ClipFlag.flagged(getClipFlags(plane, x - 1, y - 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_NE))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x - 1, y + (-1 + sizeOffset)), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_NE, ClipFlag.BW_SE) 
								|| ClipFlag.flagged(getClipFlags(plane, sizeOffset - 1 + x, y - 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_NE))
							return false;
				} else if (xOffset == 1 && yOffset == -1) {
					if (ClipFlag.flagged(getClipFlags(plane, x + size, y - 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_W, ClipFlag.BW_NW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + size, sizeOffset + (-1 + y)), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_SW) 
								|| ClipFlag.flagged(getClipFlags(plane, x + sizeOffset, y - 1), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_NE))
							return false;
				} else if (xOffset == -1 && yOffset == 1) {
					if (ClipFlag.flagged(getClipFlags(plane, x - 1, y + size), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_SE))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x - 1, y + sizeOffset), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_NE, ClipFlag.BW_SE) 
								|| ClipFlag.flagged(getClipFlags(plane, -1 + (x + sizeOffset), y + size), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SE, ClipFlag.BW_SW))
							return false;
				} else if (xOffset == 1 && yOffset == 1) {
					if (ClipFlag.flagged(getClipFlags(plane, x + size, y + size), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + sizeOffset, y + size), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SE, ClipFlag.BW_SW) 
								|| ClipFlag.flagged(getClipFlags(plane, x + size, y + sizeOffset), ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_SW))
							return false;
				}
			}
			return true;
		case NOCLIP:
			return true;
		case NORMAL:
		default:
			if (size == 1) {
				int flags = getClipFlags(plane, x + xOffset, y + yOffset);
				if (xOffset == -1 && yOffset == 0)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E);
				if (xOffset == 1 && yOffset == 0)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_W);
				if (xOffset == 0 && yOffset == -1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N);
				if (xOffset == 0 && yOffset == 1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S);
				if (xOffset == -1 && yOffset == -1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_NE) && !ClipFlag.flagged(getClipFlags(plane, x - 1, y), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E) && !ClipFlag.flagged(getClipFlags(plane, x, y - 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N);
				if (xOffset == 1 && yOffset == -1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_W, ClipFlag.BW_NW) && !ClipFlag.flagged(getClipFlags(plane, x + 1, y), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_W) && !ClipFlag.flagged(getClipFlags(plane, x, y - 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N);
				if (xOffset == -1 && yOffset == 1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_SE) && !ClipFlag.flagged(getClipFlags(plane, x - 1, y), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E) && !ClipFlag.flagged(getClipFlags(plane, x, y + 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S);
				if (xOffset == 1 && yOffset == 1)
					return !ClipFlag.flagged(flags, ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SW) && !ClipFlag.flagged(getClipFlags(plane, x + 1, y), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_W) && !ClipFlag.flagged(getClipFlags(plane, x, y + 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S);
			} else {
				if (xOffset == -1 && yOffset == 0) {
					if (ClipFlag.flagged(getClipFlags(plane, x - 1, y), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_NE) || ClipFlag.flagged(getClipFlags(plane, x - 1, -1 + (y + size)), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_SE))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x - 1, y + sizeOffset), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_NE, ClipFlag.BW_SE))
							return false;
				} else if (xOffset == 1 && yOffset == 0) {
					if (ClipFlag.flagged(getClipFlags(plane, x + size, y), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_W, ClipFlag.BW_NW) || ClipFlag.flagged(getClipFlags(plane, x + size, y - (-size + 1)), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + size, y + sizeOffset), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_SW))
							return false;
				} else if (xOffset == 0 && yOffset == -1) {
					if (ClipFlag.flagged(getClipFlags(plane, x, y - 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_NE) || ClipFlag.flagged(getClipFlags(plane, x + size - 1, y - 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_W, ClipFlag.BW_NW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + sizeOffset, y - 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_NE))
							return false;
				} else if (xOffset == 0 && yOffset == 1) {
					if (ClipFlag.flagged(getClipFlags(plane, x, y + size), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_SE) || ClipFlag.flagged(getClipFlags(plane, x + (size - 1), y + size), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size - 1; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + sizeOffset, y + size), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SE, ClipFlag.BW_SW))
							return false;
				} else if (xOffset == -1 && yOffset == -1) {
					if (ClipFlag.flagged(getClipFlags(plane, x - 1, y - 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_NE))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x - 1, y + (-1 + sizeOffset)), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_NE, ClipFlag.BW_SE) || ClipFlag.flagged(getClipFlags(plane, sizeOffset - 1 + x, y - 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_NE))
							return false;
				} else if (xOffset == 1 && yOffset == -1) {
					if (ClipFlag.flagged(getClipFlags(plane, x + size, y - 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_W, ClipFlag.BW_NW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + size, sizeOffset + (-1 + y)), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_SW) || ClipFlag.flagged(getClipFlags(plane, x + sizeOffset, y - 1), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_NE))
							return false;
				} else if (xOffset == -1 && yOffset == 1) {
					if (ClipFlag.flagged(getClipFlags(plane, x - 1, y + size), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_SE))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x - 1, y + sizeOffset), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_NE, ClipFlag.BW_SE) || ClipFlag.flagged(getClipFlags(plane, -1 + (x + sizeOffset), y + size), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SE, ClipFlag.BW_SW))
							return false;
				} else if (xOffset == 1 && yOffset == 1) {
					if (ClipFlag.flagged(getClipFlags(plane, x + size, y + size), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SW))
						return false;
					for (int sizeOffset = 1; sizeOffset < size; sizeOffset++)
						if (ClipFlag.flagged(getClipFlags(plane, x + sizeOffset, y + size), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_E, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_SE, ClipFlag.BW_SW) || ClipFlag.flagged(getClipFlags(plane, x + size, y + sizeOffset), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL, ClipFlag.BW_N, ClipFlag.BW_S, ClipFlag.BW_W, ClipFlag.BW_NW, ClipFlag.BW_SW))
							return false;
				}
			}
			return true;
		}
	}
	
	public static int getClipFlags(int plane, int x, int y) {
		return getClipFlags(new WorldTile(x, y, plane));
	}
	
	public static int getClipFlags(WorldTile tile) {
		Region region = getRegion(tile.getRegionId());
		if (region == null)
			return -1;
		return region.getClipFlags(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
	}
	
	public static final Optional<Player> containsPlayer(String username) {
		return players().filter(p -> p.getUsername().equals(username)).findAny();
	}

	public static final Player getPlayerByDisplayName(String username) {
		String formatedUsername = Utils.formatPlayerNameForDisplay(username);
		return players().filter(p -> p.getUsername().equalsIgnoreCase(formatedUsername) || p.getDisplayName().equalsIgnoreCase(formatedUsername)).findFirst().orElse(null);
	}

	public static final EntityList<Player> getPlayers() {
		return players;
	}

	public static final EntityList<NPC> getNPCs() {
		return npcs;
	}

	private World() {

	}

	public final void safeShutdown(final boolean restart, int delay) {
		if (exiting_start != 0)
			return;
		exiting_start = Utils.currentTimeMillis();
		exiting_delay = delay;
		players().forEach(p -> p.getPackets().sendSystemUpdate(delay));
		CoresManager.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					players().forEach(p -> p.realFinish());
					Launcher.shutdown();
				} catch (Throwable e) {
					Logger.handle(e);
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
		return Wilderness.isAtWild(tile);
	}

	public static void sendWorldMessage(String message, boolean forStaff) {
		players().filter(p -> (forStaff && p.getPlayerDetails().getRights() == Rights.PLAYER)).forEach(p -> p.getPackets().sendGameMessage(message));
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
		long currentTime = Utils.currentTimeMillis();
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

	public static boolean isTileFree(int plane, int x, int y, int size) {
		for (int tileX = x; tileX < x + size; tileX++)
			for (int tileY = y; tileY < y + size; tileY++)
				if (!isFloorFree(plane, tileX, tileY) || !isWallsFree(plane, tileX, tileY))
					return false;
		return true;
	}
	
	public static boolean isFloorFree(int plane, int x, int y) {
		return (getMask(plane, x, y) & (Flags.FLOOR_BLOCKSWALK | Flags.FLOORDECO_BLOCKSWALK | Flags.OBJ)) == 0;
	}
	
	public static boolean isWallsFree(int plane, int x, int y) {
		return (getMask(plane, x, y) & (Flags.CORNEROBJ_NORTHEAST | Flags.CORNEROBJ_NORTHWEST
				| Flags.CORNEROBJ_SOUTHEAST | Flags.CORNEROBJ_SOUTHWEST | Flags.WALLOBJ_EAST | Flags.WALLOBJ_NORTH
				| Flags.WALLOBJ_SOUTH | Flags.WALLOBJ_WEST)) == 0;
	}
	
	public void refreshSpawnedItems(Player player) {
		for (int regionId : player.getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getFloorItems();
			if (floorItems == null)
				continue;
			for (FloorItem item : floorItems) {
				if ((item.isInvisible() || item.isGrave()) && player != item.getOwner()
						|| item.getTile().getPlane() != player.getPlane())
					continue;
				player.getPackets().sendRemoveGroundItem(item);
			}
		}
		for (int regionId : player.getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getFloorItems();
			if (floorItems == null)
				continue;
			for (FloorItem item : floorItems) {
				if ((item.isInvisible() || item.isGrave()) && player != item.getOwner()
						|| item.getTile().getPlane() != player.getPlane())
					continue;
				player.getPackets().sendGroundItem(item);
			}
		}
	}

	public void refreshSpawnedObjects(Player player) {
		for (int regionId : player.getMapRegionsIds()) {
			List<WorldObject> spawnedObjects = World.getRegion(regionId).getSpawnedObjects();
			if (spawnedObjects != null) {
				for (WorldObject object : spawnedObjects)
					if (object.getPlane() == player.getPlane())
						player.getPackets().sendSpawnedObject(object);
			}
			List<WorldObject> removedObjects = World.getRegion(regionId).getRemovedObjects();
			if (removedObjects != null) {
				for (WorldObject object : removedObjects)
					if (object.getPlane() == player.getPlane())
						player.getPackets().sendDestroyObject(object);
			}
		}
	}
}