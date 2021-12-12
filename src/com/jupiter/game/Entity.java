package com.jupiter.game;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.jupiter.Settings;
import com.jupiter.cache.loaders.AnimationDefinitions;
import com.jupiter.cache.loaders.ObjectDefinitions;
import com.jupiter.combat.npc.MobDeath;
import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.player.Combat;
import com.jupiter.combat.player.type.CombatEffectType;
import com.jupiter.combat.player.type.PoisonType;
import com.jupiter.game.item.FloorItem;
import com.jupiter.game.map.DynamicRegion;
import com.jupiter.game.map.Region;
import com.jupiter.game.map.TileAttributes;
import com.jupiter.game.map.Vector;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.PlayerDeath;
import com.jupiter.game.player.activity.ActivityHandler;
import com.jupiter.game.route.ClipType;
import com.jupiter.game.route.Direction;
import com.jupiter.game.route.RouteFinder;
import com.jupiter.game.route.WalkStep;
import com.jupiter.game.route.strategy.DumbRouteFinder;
import com.jupiter.game.route.strategy.EntityStrategy;
import com.jupiter.game.route.strategy.FixedTileStrategy;
import com.jupiter.game.route.strategy.ObjectStrategy;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.ForceMovement;
import com.jupiter.network.encoders.other.ForceTalk;
import com.jupiter.network.encoders.other.Graphics;
import com.jupiter.network.encoders.other.Hit;
import com.jupiter.network.encoders.other.Hit.HitLook;
import com.jupiter.skills.Skills;
import com.jupiter.skills.magic.Magic;
import com.jupiter.skills.prayer.Prayer;
import com.jupiter.utility.MutableNumber;
import com.jupiter.utility.NPCBonuses;
import com.jupiter.utility.Utility;

import lombok.Getter;

public abstract class Entity extends WorldTile {

	// creates Entity and saved classes
	public Entity(WorldTile tile, EntityType type) {
		super(tile);
		this.type = requireNonNull(type);
		ifPlayer(p -> p.setHitpoints(100));
	}
	
	// transient stuff
	private transient int index;
	private transient int lastRegionId; // the last region the entity was at
	private transient WorldTile lastLoadedMapRegionTile;
	private transient ClipType clipType = ClipType.NORMAL;
	private transient CopyOnWriteArrayList<Integer> mapRegionsIds; // called by
																	// more than
																	// 1thread
																	// so
																	// concurent
	
	private transient WorldTile lastWorldTile;
	private transient WorldTile nextWorldTile;
	private transient Direction nextWalkDirection;
	private transient Direction nextRunDirection;
	private transient WorldTile nextFaceWorldTile;
	private transient boolean teleported;
	
	private transient ConcurrentLinkedQueue<Hit> receivedHits;
	private transient Map<Entity, Integer> receivedDamage;
	private transient boolean finished; // if removed
	private transient long freezeDelay;
	// entity masks
	private transient Animation nextAnimation;
	private transient Graphics nextGraphics1;
	private transient Graphics nextGraphics2;
	private transient Graphics nextGraphics3;
	private transient Graphics nextGraphics4;
	private transient ArrayList<Hit> nextHits;
	private transient ArrayList<HitBar> nextBars;
	private transient ForceMovement nextForceMovement;
	private transient ForceTalk nextForceTalk;
	private transient int nextFaceEntity;
	private transient int lastFaceEntity;
	private transient Entity attackedBy; // whos attacking you, used for single
	private transient long attackedByDelay; // delay till someone else can
											// attack you
	private transient boolean multiArea;
	private transient boolean isAtDynamicRegion;
	private transient long lastAnimationEnd;
	private transient boolean forceMultiArea;
	private transient long frozenBlocked;
	private transient long findTargetDelay;
	private transient ConcurrentHashMap<Object, Object> temporaryAttributes;
	@Getter
	private transient Movement movement;

	public transient int direction;
	private transient long tickCounter = 0;
	
	// saving stuff
	private int hitpoints;
	private transient int mapSize; // default 0, can be setted other value usefull on
							// static maps
	private transient boolean run;
	
	public boolean inArea(int a, int b, int c, int d) {
		return getX() >= a && getY() >= b && getX() <= c && getY() <= d;
	}

	public final void initEntity() {
		mapRegionsIds = new CopyOnWriteArrayList<Integer>();
		receivedHits = new ConcurrentLinkedQueue<Hit>();
		receivedDamage = new ConcurrentHashMap<Entity, Integer>();
		temporaryAttributes = new ConcurrentHashMap<Object, Object>();
		nextHits = new ArrayList<Hit>();
		nextBars = new ArrayList<HitBar>();
		nextWalkDirection = nextRunDirection = null;
		lastFaceEntity = -1;
		nextFaceEntity = -2;
		movement = new Movement(this);
	}

	public int getClientIndex() {
		return index + (this instanceof Player ? 32768 : 0);
	}

	public void applyHit(Hit hit) {
		if (isDead())
			return;
		receivedHits.add(hit); 
		handleIngoingHit(hit);
	}

	public abstract void handleIngoingHit(Hit hit);

	public void reset(boolean attributes) {
		setHitpoints(getMaxHitpoints());
		receivedHits.clear();
		resetCombat();
		getMovement().getWalkSteps().clear();
		resetReceivedDamage();
		if (attributes)
			temporaryAttributes.clear();
		if (attributes && isPlayer()) {
			toPlayer().getSkills().refreshHitPoints();
			toPlayer().getHintIconsManager().removeAll();
			toPlayer().getSkills().restoreAllSkills();
			toPlayer().getCombatDefinitions().resetSpecialAttack();
			toPlayer().getPrayer().reset();
			toPlayer().getCombatDefinitions().resetSpells(true);
			toPlayer().setResting(false);
			toPlayer().getPoisonDamage().set(0);
			toPlayer().setCastedVeng(false);
			toPlayer().getPlayerDetails().setRunEnergy(100);
			toPlayer().getAppearence().getAppeareanceBlocks();
		}
		
	}

	public void reset() {
		reset(true);
		ifNpc(npc -> {
			npc.direction = npc.getRespawnDirection();
			npc.getCombat().reset();
			npc.setBonuses(NPCBonuses.getBonuses(npc.getId())); // back to real bonuses
			npc.setForceWalk(null);
		});
	}

	public void resetCombat() {
		attackedBy = null;
		attackedByDelay = 0;
		freezeDelay = 0;
	}

	public void processReceivedHits() {
		ifPlayer(p -> {
			if (p.getMovement().getLockDelay() > Utility.currentTimeMillis() ||
				p.getNextEmoteEnd() >= Utility.currentTimeMillis())
				return;
		});
		Hit hit;
		int count = 0;
		while ((hit = receivedHits.poll()) != null && count++ < 10)
			processHit(hit);
	}

	private void processHit(Hit hit) {
		if (isDead())
			return;
		removeHitpoints(hit);
		nextHits.add(hit);
		if (nextBars.isEmpty())
			addHitBars();
	}

	public void fakeHit(Hit hit) {
		nextHits.add(hit);
		if (nextBars.isEmpty())
			addHitBars();
	}

	public void addHitBars() {
		nextBars.add(new EntityHitBar(this));
	}

	public void removeHitpoints(Hit hit) {
		if (isDead() || hit.getLook() == HitLook.ABSORB_DAMAGE)
			return;
		if (hit.getLook() == HitLook.HEALED_DAMAGE) {
			heal(hit.getDamage());
			return;
		}
		if (hit.getDamage() > hitpoints)
			hit.setDamage(hitpoints);
		addReceivedDamage(hit.getSource(), hit.getDamage());
		setHitpoints(hitpoints - hit.getDamage());
		if (hitpoints <= 0) {
			sendDeath(hit.getSource()); 
			return;
		}
		if (this instanceof Player) {
			Player player = (Player) this;
			if (player.getEquipment().getRingId() == 2550) {
				if (hit.getSource() != null && hit.getSource() != player)
					hit.getSource().applyHit(new Hit(player, (int) (hit.getDamage() * 0.1), HitLook.REFLECTED_DAMAGE));
			}
			if (player.getPrayer().hasPrayersOn()) {
				if ((hitpoints < player.getMaxHitpoints() * 0.1) && player.getPrayer().active(Prayer.REDEMPTION)) {
					setNextGraphics(new Graphics(436));
					setHitpoints((int) (hitpoints + player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5));
					player.getSkills().set(Skills.PRAYER, 0);
					player.getPrayer().setPoints(0);
				} else if (player.getEquipment().getAmuletId() != 11090 && player.getEquipment().getRingId() == 11090 && player.getHitpoints() <= player.getMaxHitpoints() * 0.1) {
					Magic.sendNormalTeleportSpell(player, 1, 0, Settings.RESPAWN_PLAYER_LOCATION);
					player.getEquipment().deleteItem(11090, 1);
					player.getPackets().sendGameMessage("Your ring of life saves you, but is destroyed in the process.");
				}
			}
			if (player.getEquipment().getAmuletId() == 11090 && player.getHitpoints() <= player.getMaxHitpoints() * 0.2) {// priority
																															// over
																															// ring
																															// of
																															// life
				player.heal((int) (player.getMaxHitpoints() * 0.3));
				player.getEquipment().deleteItem(11090, 1);
				player.getPackets().sendGameMessage("Your pheonix necklace heals you, but is destroyed in the process.");
			}
		}
		ifPlayer(p -> p.getSkills().refreshHitPoints());
	}

	public void resetReceivedDamage() {
		receivedDamage.clear();
	}

	public void removeDamage(Entity entity) {
		receivedDamage.remove(entity);
	}

	public Player getMostDamageReceivedSourcePlayer() {
		Player player = null;
		int damage = -1;
		for (Entity source : receivedDamage.keySet()) {
			if (!(source instanceof Player))
				continue;
			Integer d = receivedDamage.get(source);
			if (d == null || source.hasFinished()) {
				receivedDamage.remove(source);
				continue;
			}
			if (d > damage) {
				player = (Player) source;
				damage = d;
			}
		}
		return player;
	}

	public void processReceivedDamage() {
		for (Entity source : receivedDamage.keySet()) {
			Integer damage = receivedDamage.get(source);
			if (damage == null || source.hasFinished()) {
				receivedDamage.remove(source);
				continue;
			}
			damage--;
			if (damage == 0) {
				receivedDamage.remove(source);
				continue;
			}
			receivedDamage.put(source, damage);
		}
	}

	public void addReceivedDamage(Entity source, int amount) {
		if (source == null)
			return;
		Integer damage = receivedDamage.get(source);
		damage = damage == null ? amount : damage + amount;
		if (damage < 0)
			receivedDamage.remove(source);
		else
			receivedDamage.put(source, damage);
	}

	public void heal(int ammount) {
		heal(ammount, 0);
		ifPlayer(p -> p.getSkills().refreshHitPoints());
	}

	public void heal(int ammount, int extra) {
		setHitpoints((hitpoints + ammount) >= (getMaxHitpoints() + extra) ? (getMaxHitpoints() + extra) : (hitpoints + ammount));
	}

	public boolean hasWalkSteps() {
		return !getMovement().getWalkSteps().isEmpty();
	}

	public void sendDeath(Entity source) {
		ifPlayer(p -> World.get().submit(new PlayerDeath(p)));
		ifNpc(npc -> World.get().submit(new MobDeath(npc)));
	}
	

	public void updateAngle(WorldTile base, int sizeX, int sizeY) {
		WorldTile from = nextWorldTile != null ? nextWorldTile : this;
		int srcX = (from.getX() * 512) + (getSize() * 256);
		int srcY = (from.getY() * 512) + (getSize() * 256);
		int dstX = (base.getX() * 512) + (sizeX * 256);
		int dstY = (base.getY() * 512) + (sizeY * 256);
		int deltaX = srcX - dstX;
		int deltaY = srcY - dstY;
		direction = deltaX != 0 || deltaY != 0 ? (int) (Math.atan2(deltaX, deltaY) * 2607.5945876176133) & 0x3FFF : 0;
	}
	
	public void processMovement() {
		boolean run = this.run;
		Player player = this instanceof Player ? (Player) this : null;
		lastWorldTile = new WorldTile(this);
		if (lastFaceEntity >= 0) {
			Entity target = lastFaceEntity >= 32768 ? World.getPlayers().get(lastFaceEntity - 32768) : World.getNPCs().get(lastFaceEntity);
			if (target != null) {
				int size = target.getSize();
				updateAngle(target, size, size);
			}
		}
		nextWalkDirection = nextRunDirection = null;
		if (nextWorldTile != null) {
			setLocation(nextWorldTile);
			nextWorldTile = null;
			teleported = true;
			if (player != null && player.getTemporaryMovementType() == -1)
				player.setTemporaryMovementType(Movement.TELE_MOVE_TYPE);
			updateEntityRegion(this);
			if (needMapUpdate())
				loadMapRegions();
			resetWalkSteps();
			return;
		}
		teleported = false;
		if (getMovement().getWalkSteps().isEmpty())
			return;
		
		if (player != null) {
			if (getMovement().getWalkSteps().size() <= 1)
				player.setTemporaryMovementType(Movement.WALK_MOVE_TYPE);
		}
		for (int stepCount = 0; stepCount < (run ? 2 : 1); stepCount++) {
			WalkStep nextStep = getNextWalkStep();
			if (nextStep == null)
				break;
			if ((nextStep.checkClip() && !TileAttributes.checkWalkStep(getPlane(), getX(), getY(), nextStep.getDir(), getSize(), getClipType()))) {
				resetWalkSteps();
				break;
			}
			if (stepCount == 0)
				nextWalkDirection = nextStep.getDir();
			else
				nextRunDirection = nextStep.getDir();
			moveLocation(nextStep.getDir().getDx(), nextStep.getDir().getDy(), 0);
			if (run && stepCount == 0) { // fixes impossible steps
				WalkStep previewStep = previewNextWalkStep();
				if (previewStep == null)
					break;
				if (Utility.getPlayerRunningDirection(nextStep.getDir().getDx() + previewStep.getDir().getDx(), nextStep.getDir().getDy() + previewStep.getDir().getDy()) == -1)
					break;
			}
		}
		updateEntityRegion(this);
		if (needMapUpdate())
			loadMapRegions();
	}

	private WalkStep previewNextWalkStep() {
		WalkStep step = getMovement().getWalkSteps().peek();
		if (step == null)
			return null;
		return step;
	}
	
	@Override
	public void moveLocation(int xOffset, int yOffset, int planeOffset) {
		super.moveLocation(xOffset, yOffset, planeOffset);
		direction = Utility.getFaceDirection(xOffset, yOffset);
	}

	private boolean needMapUpdate() {
		int lastMapRegionX = lastLoadedMapRegionTile.getChunkX();
		int lastMapRegionY = lastLoadedMapRegionTile.getChunkY();
		int regionX = getChunkX();
		int regionY = getChunkY();
		int size = ((Settings.MAP_SIZES[mapSize] >> 3) / 2) - 1;
		return Math.abs(lastMapRegionX - regionX) >= size || Math.abs(lastMapRegionY - regionY) >= size;
	}

	/*
	 * returns if cliped
	 */
	public boolean clipedProjectile(WorldTile tile, boolean checkClose) {
		if (tile instanceof NPC) {
			NPC n = (NPC) tile;
			if (this instanceof Player)
				return n.clipedProjectile(this, checkClose);
			tile = n.getMiddleWorldTile();
		} else if (tile instanceof Player && this instanceof Player) {
			Player p = (Player) tile;
			return clipedProjectile(tile, checkClose, 1) || p.clipedProjectile(this, checkClose, 1);
		}
		return clipedProjectile(tile, checkClose, 1); // size 1 thats arrow
														// size, the tile has to
														// be target center
														// coord not base
	}

	/*
	 * returns if cliped
	 */
	public boolean clipedProjectile(WorldTile tile, boolean checkClose, int size) {
		int myX = getX();
		int myY = getY();
		if (this instanceof NPC && size == 1) {
			NPC n = (NPC) this;
			WorldTile thist = n.getMiddleWorldTile();
			myX = thist.getX();
			myY = thist.getY();
		}
		int destX = tile.getX();
		int destY = tile.getY();
		int lastTileX = myX;
		int lastTileY = myY;
		while (true) {
			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			int dir = Utility.getMoveDirection(myX - lastTileX, myY - lastTileY);
			if (dir == -1)
				return false;
			if (checkClose) {
//				if (!World.checkWalkStep(getPlane(), lastTileX, lastTileY, dir, size))
//					return false;
			} else if (!TileAttributes.checkProjectileStep(getPlane(), lastTileX, lastTileY, dir, size))
				return false;
			lastTileX = myX;
			lastTileY = myY;
			if (lastTileX == destX && lastTileY == destY)
				return true;
		}
	}
	
	private int[] getLastWalkTile() {
		Object[] steps = (Object[]) getMovement().getWalkSteps().toArray();
		if (steps.length == 0)
			return new int[] { getX(), getY() };
		WalkStep step = (WalkStep) steps[steps.length - 1];
		return new int[] { step.getX(), step.getY() };
	}

	public boolean addWalkSteps(final int destX, final int destY, int maxStepsCount) {
		return addWalkSteps(destX, destY, maxStepsCount, true, false);
	}

	public boolean addWalkSteps(final int destX, final int destY, int maxStepsCount, boolean clip) {
		return addWalkSteps(destX, destY, maxStepsCount, clip, false);
	}
	
	public boolean addWalkSteps(final int destX, final int destY, int maxStepsCount, boolean clip, boolean force) {
		int[] lastTile = getLastWalkTile();
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		while (true) {
			stepCount++;
			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			if (!addWalkStep(myX, myY, lastTile[0], lastTile[1], clip, force))
				return false;
			if (stepCount == maxStepsCount)
				return true;
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY)
				return true;
		}
	}
	
	public boolean addWalkStep(int nextX, int nextY, int lastX, int lastY, boolean check) {
		return addWalkStep(nextX, nextY, lastX, lastY, check, true);
	} 

	public boolean calcFollow(WorldTile target, int maxStepsCount, boolean calculate, boolean intelligent) {
		if (intelligent) {
			int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), target instanceof WorldObject ? new ObjectStrategy((WorldObject) target) : target instanceof Entity ? new EntityStrategy((Entity) target) : new FixedTileStrategy(target.getX(), target.getY()), true);
			if (steps == -1)
				return false;
			if (steps == 0)
				return true;
			int[] bufferX = RouteFinder.getLastPathBufferX();
			int[] bufferY = RouteFinder.getLastPathBufferY();
			for (int step = steps - 1; step >= 0; step--) {
				if (!addWalkSteps(bufferX[step], bufferY[step], 25, true, true))
					break;
			}
			return true;
		}
		return DumbRouteFinder.addDumbPathfinderSteps(this, target, getClipType());
	}
	
	public boolean addWalkStep(int nextX, int nextY, int lastX, int lastY, boolean check, boolean force) {
		Direction dir = Direction.forDelta(nextX - lastX, nextY - lastY);
		if (dir == null)
			return false;
		if (!force && check && !TileAttributes.checkWalkStep(getPlane(), lastX, lastY, dir, getSize(), getClipType()))// double
			return false;
		ifPlayer(player -> {
			if (!ActivityHandler.execute((Player) this, activity -> activity.checkWalkStep((Player) this, lastX, lastY, nextX, nextY)))
				return;
		});
		getMovement().getWalkSteps().add(new WalkStep(dir, nextX, nextY, check));
		return true;
	}
	
	public boolean addWalkSteps(int destX, int destY) {
		return addWalkSteps(destX, destY, -1, true);
	}
	
	public boolean calcFollow(WorldTile target, boolean inteligent) {
		return calcFollow(target, -1, true, inteligent);
	}
	
	public boolean lineOfSightTo(WorldTile tile, boolean melee) {
		if (tile instanceof NPC) {
			NPC npc = (NPC) tile;
			switch(npc.getId()) {
			case 2440:
			case 2443:
			case 2446:
			case 7567:
			case 3777:
			case 9712:
			case 9710:
			case 706:
				return true;
			}
			switch(npc.getName()) {
			case "Xuan":
			case "Fishing spot":
			case "Fishing Spot":
			case "Cavefish shoal":
			case "Rocktail shoal":
				return true;
			}
		}
		if (melee && !(tile instanceof Entity ? ((Entity) tile).ignoreWallsWhenMeleeing() : false))
			return TileAttributes.checkMeleeStep(this, tile) && TileAttributes.hasLineOfSight(getMiddleWorldTile(), tile instanceof Entity ? ((Entity) tile).getMiddleWorldTile() : tile);
		return TileAttributes.hasLineOfSight(getMiddleWorldTile(), tile instanceof Entity ? ((Entity) tile).getMiddleWorldTile() : tile);
	}
	
	public boolean ignoreWallsWhenMeleeing() {
		return false;
	}

	public void resetWalkSteps() {
		getMovement().getWalkSteps().clear();
	}

	public boolean restoreHitPoints() {
		int maxHp = getMaxHitpoints();
		if (hitpoints > maxHp) {
			setHitpoints(hitpoints - 1);
			return true;
		} else if (hitpoints < maxHp) {
			setHitpoints(hitpoints + 1);
			return true;
		}
		return false;
	}

	public long getTickCounter() {
		return tickCounter;
	}
	
	public boolean needMasksUpdate() {
		if (isPlayer())
			return (toPlayer().getTemporaryMovementType() != -1)
					|| (toPlayer().isUpdateMovementType()) || nextFaceEntity != -2 || nextAnimation != null || nextGraphics1 != null || nextGraphics2 != null
							|| nextGraphics3 != null || nextGraphics4 != null
							|| (nextWalkDirection == null && nextFaceWorldTile != null) || !nextHits.isEmpty()
							|| nextForceMovement != null || nextForceTalk != null;
		if (isNPC())
			return (toNPC().getNextTransformation() != null)|| nextFaceEntity != -2 || nextAnimation != null || nextGraphics1 != null || nextGraphics2 != null
					|| nextGraphics3 != null || nextGraphics4 != null
					|| (nextWalkDirection == null && nextFaceWorldTile != null) || !nextHits.isEmpty()
					|| nextForceMovement != null || nextForceTalk != null;
		return false;
	}

	/**
	 * Determines if this entity is dead or not.
	 * @return {@code true} if this entity is dead, {@code false} otherwise.
	 */
	public final boolean isDead() {
		return dead;
	}
	
	/**
	 * Sets the value for {@link Actor#dead}.
	 * @param dead the new value to set.
	 */
	public void setDead(boolean dead) {
		this.dead = dead;
	}
	
	/**
	 * The flag determining if this entity is dead.
	 */
	private transient boolean dead;
	
	public void resetMasks() {
		nextAnimation = null;
		nextGraphics1 = null;
		nextGraphics2 = null;
		nextGraphics3 = null;
		nextGraphics4 = null;
		if (nextWalkDirection == null)
			nextFaceWorldTile = null;
		nextForceMovement = null;
		nextForceTalk = null;
		nextFaceEntity = -2;
		nextHits.clear();
		nextBars.clear();
		ifPlayer(p -> {
			p.setTemporaryMovementType((byte) -1);
			p.setUpdateMovementType(false);
			if (!p.isClientLoadedMapRegion()) {
				p.setClientLoadedMapRegion(true);
				WorldObject.refreshSpawnedObjects(p);
				FloorItem.refreshSpawnedItems(p);
			}
		});
		ifNpc(npc -> {
			npc.setNextTransformation(null);
			npc.setChangedCombatLevel(false);
			npc.setChangedName(false);
		});
	}

	public abstract void finish();

	public int getMaxHitpoints() {
		return isNPC() ? toNPC().getCombatDefinitions().getHitpoints() : toPlayer().getSkills().getLevel(Skills.HITPOINTS) * 10 + toPlayer().getEquipment().getEquipmentHpIncrease();
	}

	public void processEntity() {
		tickCounter++;
		processMovement();
		processReceivedHits();
		processReceivedDamage();
		ifNpc(npc -> npc.processNPC());
	}

	public void loadMapRegions() {
		boolean wasAtDynamicRegion = isAtDynamicRegion();
		mapRegionsIds.clear();
		isAtDynamicRegion = false;
		int chunkX = getChunkX();
		int chunkY = getChunkY();
		int mapHash = Settings.MAP_SIZES[mapSize] >> 4;
		int minRegionX = (chunkX - mapHash) / 8;
		int minRegionY = (chunkY - mapHash) / 8;
		for (int xCalc = minRegionX < 0 ? 0 : minRegionX; xCalc <= ((chunkX + mapHash) / 8); xCalc++)
			for (int yCalc = minRegionY < 0 ? 0 : minRegionY; yCalc <= ((chunkY + mapHash) / 8); yCalc++) {
				int regionId = yCalc + (xCalc << 8);
				if (World.getRegion(regionId, this instanceof Player) instanceof DynamicRegion)
					isAtDynamicRegion = true;
				mapRegionsIds.add(regionId);
			}
		lastLoadedMapRegionTile = new WorldTile(this);
		ifPlayer(p -> {
			p.setClientLoadedMapRegion(false);
			if (p.isAtDynamicRegion()) {
				p.getPackets().sendDynamicMapRegion(!p.isStarted());
				if (!wasAtDynamicRegion)
					p.getLocalNPCUpdate().reset();
			} else {
				p.getPackets().sendMapRegion(!p.isStarted());
				if (wasAtDynamicRegion)
					p.getLocalNPCUpdate().reset();
			}
			p.setForceNextMapLoadRefresh(false);
		});
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public int getHitpoints() {
		return hitpoints;
	}

	public void setHitpoints(int hitpoints) {
		this.hitpoints = hitpoints;
	}

	public void setLastRegionId(int lastRegionId) {
		this.lastRegionId = lastRegionId;
	}

	public int getLastRegionId() {
		return lastRegionId;
	}

	public int getMapSize() {
		return mapSize;
	}

	public void setMapSize(int size) {
		this.mapSize = size;
		loadMapRegions();
	}

	public CopyOnWriteArrayList<Integer> getMapRegionsIds() {
		return mapRegionsIds;
	}

	public void setNextAnimation(Animation nextAnimation) {
		if (nextAnimation != null && nextAnimation.getIds()[0] >= 0)
			lastAnimationEnd = Utility.currentTimeMillis() + AnimationDefinitions.getAnimationDefinitions(nextAnimation.getIds()[0]).getEmoteTime();
		this.nextAnimation = nextAnimation;
	}

	public void setNextAnimationNoPriority(Animation nextAnimation) {
		if (lastAnimationEnd > Utility.currentTimeMillis())
			return;
		setNextAnimation(nextAnimation);
	}

	public Animation getNextAnimation() {
		return nextAnimation;
	}

	public void setNextGraphics(Graphics nextGraphics) {
		if (nextGraphics == null) {
			if (nextGraphics4 != null)
				nextGraphics4 = null;
			else if (nextGraphics3 != null)
				nextGraphics3 = null;
			else if (nextGraphics2 != null)
				nextGraphics2 = null;
			else
				nextGraphics1 = null;
		} else {
			if (nextGraphics.equals(nextGraphics1) || nextGraphics.equals(nextGraphics2) || nextGraphics.equals(nextGraphics3) || nextGraphics.equals(nextGraphics4))
				return;
			if (nextGraphics1 == null)
				nextGraphics1 = nextGraphics;
			else if (nextGraphics2 == null)
				nextGraphics2 = nextGraphics;
			else if (nextGraphics3 == null)
				nextGraphics3 = nextGraphics;
			else
				nextGraphics4 = nextGraphics;
		}
	}

	public Graphics getNextGraphics1() {
		return nextGraphics1;
	}

	public Graphics getNextGraphics2() {
		return nextGraphics2;
	}

	public Graphics getNextGraphics3() {
		return nextGraphics3;
	}

	public Graphics getNextGraphics4() {
		return nextGraphics4;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean hasFinished() {
		return finished;
	}

	public void setNextWorldTile(WorldTile nextWorldTile) {
		this.nextWorldTile = nextWorldTile;
	}

	public WorldTile getNextWorldTile() {
		return nextWorldTile;
	}

	public boolean hasTeleported() {
		return teleported;
	}

	public WorldTile getLastLoadedMapRegionTile() {
		return lastLoadedMapRegionTile;
	}

	public Direction getNextWalkDirection() {
		return nextWalkDirection;
	}

	public Direction getNextRunDirection() {
		return nextRunDirection;
	}

	public void setRun(boolean run) {
		this.run = run;
		ifPlayer(p -> {
			if (run != p.getRun()) {
				p.setRun(run);
				p.setUpdateMovementType(true);
				p.getMovement().sendRunButtonConfig();
			}
		});
	}

	public boolean getRun() {
		return run;
	}

	public WorldTile getNextFaceWorldTile() {
		return nextFaceWorldTile;
	}

	public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
		if (nextFaceWorldTile.getX() == getX() && nextFaceWorldTile.getY() == getY())
			return;
		this.nextFaceWorldTile = nextFaceWorldTile;
		if (nextWorldTile != null)
			direction = Utility.getFaceDirection(nextFaceWorldTile.getX() - nextWorldTile.getX(), nextFaceWorldTile.getY() - nextWorldTile.getY());
		else
			direction = Utility.getFaceDirection(nextFaceWorldTile.getX() - getX(), nextFaceWorldTile.getY() - getY());
	}

	public int getSize() {
		return isNPC() ? toNPC().getDefinitions().size : toPlayer().getAppearence().getSize();
	}

	public void cancelFaceEntityNoCheck() {
		nextFaceEntity = -2;
		lastFaceEntity = -1;
	}

	public void setNextFaceEntity(Entity entity) {
		if (entity == null) {
			nextFaceEntity = -1;
			lastFaceEntity = -1;
		} else {
			nextFaceEntity = entity.getClientIndex();
			lastFaceEntity = nextFaceEntity;
		}
	}

	public int getNextFaceEntity() {
		return nextFaceEntity;
	}

	public long getFreezeDelay() {
		return freezeDelay; // 2500 delay
	}

	public int getLastFaceEntity() {
		return lastFaceEntity;
	}

	public long getFrozenBlockedDelay() {
		return frozenBlocked;
	}

	public void setFrozeBlocked(int time) {
		this.frozenBlocked = time;
	}

	public void setFreezeDelay(int time) {
		this.freezeDelay = time;
	}

	public void addFrozenBlockedDelay(int time) {
		frozenBlocked = time + Utility.currentTimeMillis();
	}

	public void addFreezeDelay(long time) {
		addFreezeDelay(time, false);
	}

	public void addFreezeDelay(long time, boolean entangleMessage) {
		long currentTime = Utility.currentTimeMillis();
		if (currentTime > freezeDelay) {
			resetWalkSteps();
			freezeDelay = time + currentTime;
			if (this instanceof Player) {
				Player p = (Player) this;
				if (!entangleMessage)
					p.getPackets().sendGameMessage("You have been frozen.");
			}
		}
	}

	public double getMagePrayerMultiplier() {
		return isPlayer() ? 0.6 : 0;
	}

	public double getRangePrayerMultiplier() {
		return isPlayer() ? 0.6 : 0;
	}

	public double getMeleePrayerMultiplier() {
		return isPlayer() ? 0.6 : 0;
	}

	public Entity getAttackedBy() {
		return attackedBy;
	}

	public void setAttackedBy(Entity attackedBy) {
		this.attackedBy = attackedBy;
	}

	public long getAttackedByDelay() {
		return attackedByDelay;
	}

	public void setAttackedByDelay(long attackedByDelay) {
		this.attackedByDelay = attackedByDelay;
	}

	public void checkMultiArea() {
		multiArea = forceMultiArea ? true : World.isMultiArea(this);
		ifPlayer(p -> {
			if (!p.isStarted())
				return;
			boolean isAtMultiArea = p.isForceMultiArea() ? true : World.isMultiArea(this);
			if (isAtMultiArea && !p.isAtMultiArea()) {
				p.setAtMultiArea(isAtMultiArea);
				p.getPackets().sendGlobalConfig(616, 1);
			} else if (!isAtMultiArea && p.isAtMultiArea()) {
				p.setAtMultiArea(isAtMultiArea);
				p.getPackets().sendGlobalConfig(616, 0);
			}
		});
	}

	public boolean isAtMultiArea() {
		return multiArea;
	}

	public void setAtMultiArea(boolean multiArea) {
		this.multiArea = multiArea;
	}

	public boolean isAtDynamicRegion() {
		return isAtDynamicRegion;
	}

	public ForceMovement getNextForceMovement() {
		return nextForceMovement;
	}

	public void setNextForceMovement(ForceMovement nextForceMovement) {
		this.nextForceMovement = nextForceMovement;
	}

	public ForceTalk getNextForceTalk() {
		return nextForceTalk;
	}

	public void setNextForceTalk(ForceTalk nextForceTalk) {
		this.nextForceTalk = nextForceTalk;
	}

	public void faceEntity(Entity target) {
		setNextFaceWorldTile(new WorldTile(target.getCoordFaceX(target.getSize()), target.getCoordFaceY(target.getSize()), target.getPlane()));
	}

	public void faceObject(WorldObject object) {
		ObjectDefinitions objectDef = object.getDefinitions();
		setNextFaceWorldTile(new WorldTile(object.getCoordFaceX(objectDef.getSizeX(), objectDef.getSizeY(), object.getRotation()), object.getCoordFaceY(objectDef.getSizeX(), objectDef.getSizeY(), object.getRotation()), object.getPlane()));
	}

	public long getLastAnimationEnd() {
		return lastAnimationEnd;
	}

	public ConcurrentHashMap<Object, Object> getTemporaryAttributtes() {
		return temporaryAttributes;
	}

	public boolean isForceMultiArea() {
		return forceMultiArea;
	}

	public void setForceMultiArea(boolean forceMultiArea) {
		this.forceMultiArea = forceMultiArea;
		checkMultiArea();
	}

	public WorldTile getLastWorldTile() {
		return lastWorldTile;
	}

	public ArrayList<Hit> getNextHits() {
		return nextHits;
	}

	public void playSound(int soundId, int type) {
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				World.players().filter(p -> !withinDistance(p)).forEach(p -> p.getPackets().sendSound(soundId, 0, type));
			}
		}
	}

	public long getFindTargetDelay() {
		return findTargetDelay;
	}

	public void setFindTargetDelay(long findTargetDelay) {
		this.findTargetDelay = findTargetDelay;
	}

	private WalkStep getNextWalkStep() {
		WalkStep step = getMovement().getWalkSteps().poll();
		if (step == null)
			return null;
		return step;
	}

	public ArrayList<HitBar> getNextBars() {
		return nextBars;
	}

	public void setNextBars(ArrayList<HitBar> nextBars) {
		this.nextBars = nextBars;
	}

	/**
	 * The amount of poison damage this entity has.
	 */
	private final MutableNumber poisonDamage = new MutableNumber();
	
	/**
	 * Determines if this entity is poisoned.
	 * @return {@code true} if this entity is poisoned, {@code false}
	 * otherwise.
	 */
	public final boolean isPoisoned() {
		return poisonDamage.get() > 0;
	}
	
	/**
	 * The type of poison that was previously applied.
	 */
	private PoisonType poisonType;
	
	/**
	 * Gets the type of poison that was previously applied.
	 * @return the type of poison.
	 */
	public PoisonType getPoisonType() {
		return poisonType;
	}
	
	/**
	 * Applies poison with an intensity of {@code type} to the entity.
	 * @param type the poison type to apply.
	 */
	public void poison(PoisonType type) {
		poisonType = type;
		if (this instanceof Player) {
			Player player = (Player) this;
			player.getPackets().sendGameMessage("You have been poisoned!");
		}
		Combat.effect(this, CombatEffectType.POISON);
	}
	
	/**
	 * Sets the value for {@link Actor#poisonType}.
	 * @param poisonType the new value to set.
	 */
	public void setPoisonType(PoisonType poisonType) {
		this.poisonType = poisonType;
	}

	/**
	 * Gets the amount of poison damage this entity has.
	 * @return the amount of poison damage.
	 */
	public final MutableNumber getPoisonDamage() {
		return poisonDamage;
	}
	
	/*
	 * check if the entity region changed because moved or teled then we update it
	 */
	public final void updateEntityRegion(Entity entity) {
		if (entity.hasFinished()) {
			if (entity instanceof Player)
				World.getRegion(entity.getLastRegionId()).removePlayerIndex(entity.getIndex());
			else
				World.getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
			return;
		}
		int regionId = entity.getRegionId();
		if (entity.getLastRegionId() != regionId) { // map region entity at
			// changed
			if (entity instanceof Player) {
				if (entity.getLastRegionId() > 0)
					World.getRegion(entity.getLastRegionId()).removePlayerIndex(entity.getIndex());
				Region region = World.getRegion(regionId);
				region.addPlayerIndex(entity.getIndex());
				Player player = (Player) entity;
				int musicId = region.getMusicId();
				if (musicId != -1)
					player.getMusicsManager().checkMusic(musicId);
				ActivityHandler.executeVoid(player, activity -> activity.moved(player));
				if (player.isStarted()) {
//					checkControlersAtMove(player);
				}
			} else {
				if (entity.getLastRegionId() > 0)
					World.getRegion(entity.getLastRegionId()).removeNPCIndex(entity.getIndex());
				World.getRegion(regionId).addNPCIndex(entity.getIndex());
			}
			entity.checkMultiArea();
			entity.setLastRegionId(regionId);
		} else {
			ifPlayer(player -> {
				ActivityHandler.executeVoid(player, activity -> activity.moved(player));
//				if (player.isStarted())
//					checkControlersAtMove(player);
			});
			entity.checkMultiArea();
		}
	}

	/**
	 * Gets the center location.
	 *
	 * @return The center location.
	 */
	public WorldTile getCenterLocation() {
		int offset = getSize() >> 1;
		return this.getLastWorldTile().transform(offset, offset, 0);
	}
	
	/**
	 * The type of node that this node is.
	 */
	@Getter
	private final EntityType type;

	/**
	 * Determines if this entity is a {@link Player}.
	 * 
	 * @return {@code true} if this entity is a {@link Player}, {@code false}
	 *         otherwise.
	 */
	public final boolean isPlayer() {
		return getType() == EntityType.PLAYER;
	}

	/**
	 * Executes the specified action if the underlying node is a player.
	 * 
	 * @param action the action to execute.
	 */
	public final void ifPlayer(Consumer<Player> action) {
		if (!this.isPlayer()) {
			return;
		}
		action.accept(this.toPlayer());
	}

	/**
	 * Casts the {@link Actor} to a {@link Player}.
	 * 
	 * @return an instance of this {@link Actor} as a {@link Player}.
	 */
	public final Player toPlayer() {
		Preconditions.checkArgument(isPlayer(), "Cannot cast this entity to player.");
		return (Player) this;
	}

	/**
	 * Determines if this entity is a {@link Mob}.
	 * 
	 * @return {@code true} if this entity is a {@link Mob}, {@code false}
	 *         otherwise.
	 */
	public final boolean isNPC() {
		return getType() == EntityType.NPC;
	}

	/**
	 * Executes the specified action if the underlying node is a player.
	 * 
	 * @param action the action to execute.
	 */
	public final void ifNpc(Consumer<NPC> action) {
		if (!this.isNPC())
			return;
		action.accept(this.toNPC());
	}

	/**
	 * Casts the {@link Actor} to a {@link Mob}.
	 * 
	 * @return an instance of this {@link Actor} as a {@link Mob}.
	 */
	public final NPC toNPC() {
		Preconditions.checkArgument(isNPC(), "Cannot cast this entity to npc.");
		return (NPC) this;
	}
	
	public ClipType getClipType() {
		if (clipType == null)
			clipType = ClipType.NORMAL;
		return clipType;
	}

	public void setClipType(ClipType clipType) {
		this.clipType = clipType;
	}
	
	public WorldTile getMiddleWorldTile() {
		int size = getSize();
		return size == 1 ? this : new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane());
	}
	
	public Vector getMiddleWorldTileAsVector() {
		int size = getSize();
		if (size == 1)
			return new Vector(this);
		return new Vector(getX() + (size-1)/ 2.0f, getY() + (size-1)/ 2.0f);
	}
}