package com.jupiter.plugin.handlers;

import com.jupiter.cache.io.InputStream;
import com.jupiter.cache.loaders.ObjectDefinitions;
import com.jupiter.game.item.Item;
import com.jupiter.game.map.ObjectType;
import com.jupiter.game.map.TileAttributes;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.route.strategy.RouteEvent;
import com.jupiter.plugin.PluginManager;
import com.jupiter.plugin.events.ObjectClickEvent;
import com.jupiter.utils.LogUtility;
import com.jupiter.utils.LogUtility.Type;
import com.jupiter.utils.Utils;

public abstract class ObjectClickHandler extends PluginHandler<ObjectClickEvent> {

	private WorldTile[] tiles;
	private boolean checkDistance = true;
	private ObjectType type;

	public ObjectClickHandler(boolean checkDistance, Object[] namesOrIds, WorldTile... tiles) {
		super(namesOrIds);
		this.tiles = tiles;
		this.checkDistance = checkDistance;
	}

	public ObjectClickHandler(Object[] namesOrIds, WorldTile... tiles) {
		this(true, namesOrIds, tiles);
	}

	public ObjectClickHandler(Object[] namesOrIds) {
		this(true, namesOrIds);
	}

	public ObjectClickHandler(Object[] namesOrIds, ObjectType type) {
		this(true, namesOrIds);
		this.type = type;
	}

	public boolean isCheckDistance() {
		return checkDistance;
	}

	public WorldTile[] getTiles() {
		return tiles;
	}

	public ObjectType getType() {
		return type;
	}

	public static void handleOption(final Player player, InputStream stream, int option) {
		if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
			return;
		long currentTime = Utils.currentTimeMillis();
		if (player.getMovement().getLockDelay() >= currentTime || player.getNextEmoteEnd() >= currentTime)
			return;

		int id = getObjectIDFromStreamCopy(stream);

		int y = stream.readUnsignedShort();
		int x = stream.readUnsignedShort();
		boolean forceRun = stream.readUnsignedShort() == 1;
		if (id == -1)
			id = stream.readUnsignedShort();

		LogUtility.log(Type.INFO, "Object Click Handler", x + ", " + y + ", " + id + ", " + forceRun);

		int rotation = 0;
		if (player.isAtDynamicRegion()) {
			rotation = TileAttributes.getRotation(player.getPlane(), x, y);
			if (rotation == 1) {
				ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(id);
				y += defs.getSizeY() - 1;
			} else if (rotation == 2) {
				ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(id);
				x += defs.getSizeY() - 1;
			}
		}
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		final int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId))
			return;
		WorldObject mapObject = World.getRegion(regionId).getObject(id, tile);

		if (mapObject == null || mapObject.getId() != id)
			return;
		if (player.isAtDynamicRegion() && TileAttributes.getRotation(player.getPlane(), x, y) != 0) { // temp fix
			ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(id);
			if (defs.getSizeX() > 1 || defs.getSizeY() > 1) {
				for (int xs = 0; xs < defs.getSizeX() + 1 && (mapObject == null || mapObject.getId() != id); xs++) {
					for (int ys = 0; ys < defs.getSizeY() + 1 && (mapObject == null || mapObject.getId() != id); ys++) {
						tile.setLocation(x + xs, y + ys, tile.getPlane());
						mapObject = World.getRegion(regionId).getObject(id, tile);
					}
				}
			}
			if (mapObject == null || mapObject.getId() != id)
				return;
		}
		final WorldObject object = !player.isAtDynamicRegion() ? mapObject
				: new WorldObject(id, mapObject.getType(), (byte) (mapObject.getRotation() + rotation % 4), x, y,
						player.getPlane());
		player.getAttributes().stopAll(player, false);
		if (forceRun)
			player.setRun(forceRun);

		if (option == -1) {
			handleOptionExamine(player, object);
			return;
		}

		player.setRouteEvent(new RouteEvent(object, new Runnable() {
			@Override
			public void run() {
				player.getAttributes().stopAll(player);
				player.faceObject(object);
				PluginManager.handle(new ObjectClickEvent(player, object, option));
			}
		}, false));
	}

	private static int getObjectIDFromStreamCopy(InputStream stream) {
		try {
			InputStream stream2;
			stream2 = (InputStream) stream.clone();
			stream2.readInt();
			return stream2.readInt();
		} catch (Exception e) {
			return -1;
		}
	}

	private static void handleOptionExamine(final Player player, final WorldObject object) {
		int offsetX = object.getX() - player.getX();
		int offsetY = object.getY() - player.getY();
		LogUtility.log(Type.INFO, "Object Click Handler", "Offsets" + offsetX + " , " + offsetY);

		player.getPackets().sendGameMessage("It's an " + object.getDefinitions().name + ".");

		LogUtility.log(Type.INFO, "Object Click Handler",
				"examined object id : " + object.getId() + ", " + object.getX() + ", " + object.getY() + ", "
						+ object.getPlane() + ", " + object.getType() + ", " + object.getRotation() + ", "
						+ object.getDefinitions().name + ", varbit: " + object.getConfigByFile() + ", var: "
						+ object.getConfig());
	}

	@SuppressWarnings("unused")
	public static void handleItemOnObject(final Player player, final WorldObject object, final int interfaceId,
			final Item item) {
		final int itemId = item.getId();
		final ObjectDefinitions objectDef = object.getDefinitions();
		player.setRouteEvent(new RouteEvent(object, new Runnable() {
			@Override
			public void run() {
				player.faceObject(object);

				LogUtility.log(Type.INFO, "Object Click Handler", "Item on object: " + object.getId());
			}

		}, false));
	}
}