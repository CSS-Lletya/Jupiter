package com.jupiter.game.item;

import java.util.List;

import com.jupiter.cores.CoresManager;
import com.jupiter.game.map.Region;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.LogUtility.Type;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FloorItem extends Item {

	private WorldTile tile;
	private Player owner;
	private boolean invisible;
	private boolean grave;

	public FloorItem(int id) {
		super(id);
	}

	@Override
	public Item setAmount(int amount) {
		setAmount(amount);
		return this;
	}

	public FloorItem(Item item, WorldTile tile, Player owner, boolean underGrave, boolean invisible) {
		super(item.getId(), item.getAmount());
		this.tile = tile;
		this.owner = owner;
		grave = underGrave;
		this.invisible = invisible;
	}
	
	public boolean hasOwner() {
		return owner != null;
	}
	
	public static final void createGroundItem(final Item item, final WorldTile tile) {
		final FloorItem floorItem = new FloorItem(item, tile, null, false, false);
		final Region region = World.getRegion(tile.getRegionId());
		region.forceGetFloorItems().add(floorItem);
		int regionId = tile.getRegionId();
		World.players().filter(p -> tile.getPlane() == p.getPlane() && p.getMapRegionsIds().contains(regionId)).forEach(p -> p.getPackets().sendGroundItem(floorItem));
	}

	public static final void createGroundItem(final Item item, final WorldTile tile, final Player owner/* null for default */, final boolean underGrave, long hiddenTime/* default 3minutes */, boolean invisible) {
		createGroundItem(item, tile, owner, underGrave, hiddenTime, invisible, false, 180);
	}

	public static final void createGroundItem(final Item item, final WorldTile tile, final Player owner/* null for default */, final boolean underGrave, long hiddenTime/* default 3minutes */, boolean invisible, boolean intoGold) {
		createGroundItem(item, tile, owner, underGrave, hiddenTime, invisible, intoGold, 180);
	}

	public static final void createGroundItem(final Item item, final WorldTile tile, final Player owner/* null for default */, final boolean underGrave, long hiddenTime/* default 3minutes */, boolean invisible, boolean intoGold, final int publicTime) {
		if (intoGold) {
			if (!ItemConstants.isTradeable(item)) {
				int price = item.getDefinitions().getValue();
				if (price <= 0)
					return;
				item.setId(995);
				item.setAmount(price);
			}
		}
		final FloorItem floorItem = new FloorItem(item, tile, owner, owner == null ? false : underGrave, invisible);
		final Region region = World.getRegion(tile.getRegionId());
		region.forceGetFloorItems().add(floorItem);
		if (invisible && hiddenTime != -1) {
			if (owner != null)
				owner.getPackets().sendGroundItem(floorItem);
			CoresManager.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						if (!region.forceGetFloorItems().contains(floorItem))
							return;
						int regionId = tile.getRegionId();
						if (underGrave || !ItemConstants.isTradeable(floorItem) || item.getName().contains("Dr nabanik")) {
							region.forceGetFloorItems().remove(floorItem);
							if (owner != null) {
								if (owner.getMapRegionsIds().contains(regionId) && owner.getPlane() == tile.getPlane())
									owner.getPackets().sendRemoveGroundItem(floorItem);
							}
							return;
						}

						floorItem.setInvisible(false);
						for (Player player : World.getPlayers()) {
							if (player == null || player == owner || !player.isStarted() || player.hasFinished() || player.getPlane() != tile.getPlane() || !player.getMapRegionsIds().contains(regionId))
								continue;
							player.getPackets().sendGroundItem(floorItem);
						}
						removeGroundItem(floorItem, publicTime);
					} catch (Throwable e) {
						LogUtility.log(Type.ERROR, "Floor Item", e.getMessage());
					}
				}
			}, (int) hiddenTime);
			return;
		}
		int regionId = tile.getRegionId();
		for (Player player : World.getPlayers()) {
			if (player == null || !player.isStarted() || player.hasFinished() || player.getPlane() != tile.getPlane() || !player.getMapRegionsIds().contains(regionId))
				continue;
			player.getPackets().sendGroundItem(floorItem);
		}
		removeGroundItem(floorItem, publicTime);
	}

	public static final void updateGroundItem(Item item, final WorldTile tile, final Player owner) {
		final FloorItem floorItem = World.getRegion(tile.getRegionId()).getGroundItem(item.getId(), tile, owner);
		if (floorItem == null) {
			createGroundItem(item, tile, owner, false, 360, true);
			return;
		}
		floorItem.setAmount(floorItem.getAmount() + item.getAmount());
		owner.getPackets().sendRemoveGroundItem(floorItem);
		owner.getPackets().sendGroundItem(floorItem);

	}

	private static final void removeGroundItem(final FloorItem floorItem, long publicTime) {
		if (publicTime < 0) {
			return;
		}
		CoresManager.schedule(new Runnable() {
			@Override
			public void run() {
				try {
					int regionId = floorItem.getTile().getRegionId();
					Region region = World.getRegion(regionId);
					if (!region.forceGetFloorItems().contains(floorItem))
						return;
					region.forceGetFloorItems().remove(floorItem);
					World.players().filter(p -> p.getPlane() != floorItem.getTile().getPlane() || !p.getMapRegionsIds().contains(regionId)) .forEach(p -> p.getPackets().sendRemoveGroundItem(floorItem));
					
				} catch (Throwable e) {
					LogUtility.log(Type.ERROR, "Floor Item", e.getMessage());
				}
			}
		}, (int) publicTime);
	}

	public static final boolean removeGroundItem(Player player, FloorItem floorItem) {
		return removeGroundItem(player, floorItem, true);
	}

	public static final boolean removeGroundItem(Player player, FloorItem floorItem, boolean add) {
		int regionId = floorItem.getTile().getRegionId();
		Region region = World.getRegion(regionId);
		if (!region.forceGetFloorItems().contains(floorItem))
			return false;
		if (player.getInventory().getFreeSlots() == 0)
			return false;
		region.forceGetFloorItems().remove(floorItem);
		if (add)
			player.getInventory().addItem(floorItem.getId(), floorItem.getAmount());
		if (floorItem.isInvisible() || floorItem.isGrave()) {
			player.getPackets().sendRemoveGroundItem(floorItem);
			return true;
		} else {
			World.players().filter(p -> p.getPlane() != floorItem.getTile().getPlane() || !p.getMapRegionsIds().contains(regionId)).forEach(p -> p.getPackets().sendRemoveGroundItem(floorItem));
			return true;
		}
	}
	
	public static void refreshSpawnedItems(Player player) {
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
}