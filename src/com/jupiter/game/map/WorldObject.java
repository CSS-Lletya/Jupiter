package com.jupiter.game.map;

import java.util.List;

import com.jupiter.cache.loaders.ObjectDefinitions;
import com.jupiter.game.Entity;
import com.jupiter.game.player.Player;
import com.jupiter.net.encoders.other.Animation;

/**
 * A container class for a World Object.
 * @author Dennis
 *
 */
public class WorldObject extends WorldTile {

	/**
	 * The World Object Id.
	 */
	private final int id;
	
	/**
	 * The World Object Type.
	 */
	private final short type;
	
	/**
	 * The World Object Rotation.
	 */
	private short rotation;
	
	/**
	 * The World Object Life (like mining a Shooting star, it's life depletes when
	 * you get stardust, such.).
	 */
	private short life;

	/**
	 * Create a new World Object.
	 * @param id
	 * @param type
	 * @param rotation
	 * @param tile
	 */
	public WorldObject(int id, short type, short rotation, WorldTile tile) {
		super(tile.getX(), tile.getY(), tile.getPlane());
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
	}

	/**
	 * Create a new World Object.
	 * @param id
	 * @param type
	 * @param rotation
	 * @param x
	 * @param y
	 * @param plane
	 */
	public WorldObject(int id, short type, short rotation, int x, int y, int plane) {
		super(x, y, plane);
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = 1;
	}

	/**
	 * Create a new World Object.
	 * @param id
	 * @param type
	 * @param rotation
	 * @param x
	 * @param y
	 * @param plane
	 * @param life
	 */
	public WorldObject(int id, short type, short rotation, int x, int y, int plane, short life) {
		super(x, y, plane);
		this.id = id;
		this.type = type;
		this.rotation = rotation;
		this.life = life;
	}

	/**
	 * Create a new World Object.
	 * @param object
	 */
	public WorldObject(WorldObject object) {
		super(object.getX(), object.getY(), object.getPlane());
		this.id = object.id;
		this.type = object.type;
		this.rotation = object.rotation;
		this.life = object.life;
	}

	/**
	 * Gets the World Object Id.
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the World Object Type.
	 * @return type
	 */
	public short getType() {
		return type;
	}

	/**
	 * Gets the World Object Rotation.
	 * @return rotation
	 */
	public short getRotation() {
		return rotation;
	}

	/**
	 * Sets the World Object Rotation
	 * @param rotation
	 */
	public void setRotation(byte rotation) {
		this.rotation = rotation;
	}

	/**
	 * Gets the World Objets Life.
	 * @return life
	 */
	public short getLife() {
		return life;
	}

	/**
	 * Sets the World Object Life.
	 * @param life
	 */
	public void setLife(short life) {
		this.life = life;
	}

	/**
	 * Removes Life points from a 
	 * World Object.
	 */
	public void decrementObjectLife() {
		this.life--;
	}

	/**
	 * Gets the World Object Definition
	 * @return definition
	 */
	public ObjectDefinitions getDefinitions() {
		return ObjectDefinitions.getObjectDefinitions(getId());
	}

	public static final boolean isSpawnedObject(WorldObject object) {
		final int regionId = object.getRegionId();
		WorldObject spawnedObject = World.getRegion(regionId).getSpawnedObject(object);
		if (spawnedObject != null && object.getId() == spawnedObject.getId())
			return true;
		return false;
	}

	public static final void removeObject(WorldObject object, boolean clip) {
		int regionId = object.getRegionId();
		World.getRegion(regionId).addRemovedObject(object);
		if (clip) {
			int baseLocalX = object.getX() - ((regionId >> 8) * 64);
			int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
			World.getRegion(regionId).removeMapObject(object, baseLocalX, baseLocalY);
		}
		synchronized (World.getPlayers()) {
			World.players().forEach(p -> p.getPackets().sendDestroyObject(object));
		}
	}

	public static final WorldObject getObject(WorldTile tile) {
		int regionId = tile.getRegionId();
		int baseLocalX = tile.getX() - ((regionId >> 8) * 64);
		int baseLocalY = tile.getY() - ((regionId & 0xff) * 64);
		return World.getRegion(regionId).getObject(tile.getPlane(), baseLocalX, baseLocalY);
	}

	public static final WorldObject getObject(WorldTile tile, int type) {
		int regionId = tile.getRegionId();
		int baseLocalX = tile.getX() - ((regionId >> 8) * 64);
		int baseLocalY = tile.getY() - ((regionId & 0xff) * 64);
		return World.getRegion(regionId).getObject(tile.getPlane(), baseLocalX, baseLocalY, type);
	}

	public static final void createObject(WorldObject object, boolean clip) {
		int regionId = object.getRegionId();
		World.getRegion(regionId).addObject(object);
		if (clip) {
			int baseLocalX = object.getX() - ((regionId >> 8) * 64);
			int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
			World.getRegion(regionId).addMapObject(object, baseLocalX, baseLocalY);
		}
		synchronized (World.getPlayers()) {
			World.players().filter(p -> !p.getMapRegionsIds().contains(regionId)).forEach(p -> p.getPackets().sendSpawnedObject(object));
		}
	}

	public static final void sendObjectAnimation(WorldObject object, Animation animation) {
		sendObjectAnimation(null, object, animation);
	}

	public static final void sendObjectAnimation(Entity creator, WorldObject object, Animation animation) {
		if (creator == null) {
			World.players().filter(p -> p.withinDistance(object)).forEach(p-> p.getPackets().sendObjectAnimation(object, animation));
		} else {
			for (int regionId : creator.getMapRegionsIds()) {
				List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playersIndexes == null)
					continue;
				for (Integer playerIndex : playersIndexes) {
					Player player = World.getPlayers().get(playerIndex);
					if (player == null || !player.isStarted() || player.hasFinished() || !player.withinDistance(object))
						continue;
					player.getPackets().sendObjectAnimation(object, animation);
				}
			}
		}
	}
	
	public static void destroySpawnedObject(WorldObject object, boolean clip) {
		int regionId = object.getRegionId();
		int baseLocalX = object.getX() - ((regionId >> 8) * 64);
		int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
		WorldObject realMapObject = World.getRegion(regionId).getRealObject(object);

		World.getRegion(regionId).removeObject(object);
		if (clip)
			World.getRegion(regionId).removeMapObject(object, baseLocalX, baseLocalY);
		World.players().filter(p -> !p.getMapRegionsIds().contains(regionId)).forEach(p -> {
			if (realMapObject != null)
				p.getPackets().sendSpawnedObject(realMapObject);
			else
				p.getPackets().sendDestroyObject(object);
		});
	}

	public static void destroySpawnedObject(WorldObject object) {
		int regionId = object.getRegionId();
		int baseLocalX = object.getX() - ((regionId >> 8) * 64);
		int baseLocalY = object.getY() - ((regionId & 0xff) * 64);
		World.getRegion(regionId).removeObject(object);
		World.getRegion(regionId).removeMapObject(object, baseLocalX, baseLocalY);
		World.players().filter(p -> !p.getMapRegionsIds().contains(regionId)).forEach(p -> p.getPackets().sendDestroyObject(object));
	}

	public static final void spawnObject(WorldObject object) {
		World.getRegion(object.getRegionId()).addObject(object, object.getPlane(), object.getXInRegion(),
				object.getYInRegion());
	}

	public int getConfigByFile() {
		return getDefinitions().configFileId;
	}
	
	public int getConfig() {
		return getDefinitions().configId;
	}
	
	public boolean isAt(int x, int y) {
		return this.x == x && this.y == y;
	}
	
	public boolean isAt(int x, int y, int z) {
		return this.x == x && this.y == y && this.plane == z;
	}
}