package com.jupiter.game.map;

import com.jupiter.game.Entity;
import com.jupiter.game.route.ClipFlag;
import com.jupiter.game.route.ClipType;
import com.jupiter.game.route.Direction;
import com.jupiter.game.route.Flags;
import com.jupiter.utils.Utils;

/**
 * Defines specific attributes that a tile may contain.
 * @author Dennis
 */
public class TileAttributes {

	public static boolean floorAndWallsFree(WorldTile tile, int size) {
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				if (!floorFree(tile.transform(x, y)) || !wallsFree(tile.transform(x, y)))
					return false;
		return true;
	}

	public static boolean floorFree(WorldTile tile, int size) {
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				if (!floorFree(tile.transform(x, y)))
					return false;
		return true;
	}
	
	public static boolean floorFree(int plane, int x, int y, int size) {
		return floorFree(new WorldTile(x, y, plane), size);
	}
	
	public static boolean floorFree(WorldTile tile) {
		return !ClipFlag.flagged(getClipFlags(tile), ClipFlag.PFBW_FLOOR, ClipFlag.PFBW_GROUND_DECO, ClipFlag.BW_FULL);
	}

	public static boolean floorFree(int plane, int x, int y) {
		return floorFree(new WorldTile(x, y, plane));
	}
	
	public static boolean wallsFree(WorldTile tile) {
		return !ClipFlag.flagged(getClipFlags(tile), ClipFlag.BW_NE, ClipFlag.BW_NW, ClipFlag.BW_SE, ClipFlag.BW_SW, ClipFlag.BW_E, ClipFlag.BW_N, ClipFlag.BW_S, ClipFlag.BW_W);
	}

	public static boolean wallsFree(int plane, int x, int y) {
		return wallsFree(new WorldTile(x, y, plane));
	}

	public static int getMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = World.getRegion(regionId);
		if (region == null)
			return -1;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getMask(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static void setMask(int plane, int x, int y, int mask) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = World.getRegion(regionId);
		if (region == null)
			return;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		region.setMask(tile.getPlane(), baseLocalX, baseLocalY, mask);
	}

	public static int getRotation(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = World.getRegion(regionId);
		if (region == null)
			return 0;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getRotation(tile.getPlane(), baseLocalX, baseLocalY);
	}

	private static int getClipedOnlyMask(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		int regionId = tile.getRegionId();
		Region region = World.getRegion(regionId);
		if (region == null)
			return -1;
		int baseLocalX = x - ((regionId >> 8) * 64);
		int baseLocalY = y - ((regionId & 0xff) * 64);
		return region.getMaskClipedOnly(tile.getPlane(), baseLocalX, baseLocalY);
	}

	private static int getClipFlagsProj(int plane, int x, int y) {
		WorldTile tile = new WorldTile(x, y, plane);
		Region region = World.getRegion(tile.getRegionId());
		if (region == null)
			return -1;
		return region.getMaskClipedOnly(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
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
		Region region = World.getRegion(tile.getRegionId());
		if (region == null)
			return -1;
		return region.getMask(tile.getPlane(), tile.getXInRegion(), tile.getYInRegion());
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
}