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
import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.player.Combat;
import com.jupiter.combat.player.type.CombatEffectType;
import com.jupiter.combat.player.type.PoisonType;
import com.jupiter.game.map.DynamicRegion;
import com.jupiter.game.map.Region;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.encoders.other.ForceMovement;
import com.jupiter.net.encoders.other.ForceTalk;
import com.jupiter.net.encoders.other.Graphics;
import com.jupiter.net.encoders.other.Hit;
import com.jupiter.net.encoders.other.Hit.HitLook;
import com.jupiter.skills.Skills;
import com.jupiter.skills.magic.Magic;
import com.jupiter.utils.MutableNumber;
import com.jupiter.utils.Utils;

import lombok.Getter;

public abstract class Entity extends WorldTile {

	// creates Entity and saved classes
	public Entity(WorldTile tile, EntityType type) {
		super(tile);
		this.type = requireNonNull(type);
	}
	
	// transient stuff
	private transient int index;
	private transient int lastRegionId; // the last region the entity was at
	private transient WorldTile lastLoadedMapRegionTile;
	private transient CopyOnWriteArrayList<Integer> mapRegionsIds; // called by
																	// more than
																	// 1thread
																	// so
																	// concurent
	private transient int direction;
	private transient WorldTile lastWorldTile;
	private transient WorldTile nextWorldTile;
	private transient int nextWalkDirection;
	private transient int nextRunDirection;
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

	// saving stuff
	private int hitpoints;
	private transient int mapSize; // default 0, can be setted other value usefull on
							// static maps
	
	public boolean inArea(int a, int b, int c, int d) {
		return getX() >= a && getY() >= b && getX() <= c && getY() <= d;
	}

	public final void initEntity() {
		mapRegionsIds = new CopyOnWriteArrayList<Integer>();
		receivedHits = new ConcurrentLinkedQueue<Hit>();
		receivedDamage = new ConcurrentHashMap<Entity, Integer>();
		temporaryAttributes = new ConcurrentHashMap<Object, Object>();
		this.attributes = new ConcurrentHashMap<>();
		nextHits = new ArrayList<Hit>();
		nextBars = new ArrayList<HitBar>();
		nextWalkDirection = nextRunDirection - 1;
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
			toPlayer().getMovement().setRestingMode(false);
			toPlayer().getPoisonDamage().set(0);
			toPlayer().setCastedVeng(false);
			toPlayer().getPlayerDetails().setRunEnergy(100);
			toPlayer().getAppearence().getAppeareanceBlocks();
		}
		
	}

	public void reset() {
		reset(true);
	}

	public void resetCombat() {
		attackedBy = null;
		attackedByDelay = 0;
		freezeDelay = 0;
	}

	public void processReceivedHits() {
		if (this instanceof Player) {
			if (((Player) this).getNextEmoteEnd() >= Utils.currentTimeMillis() || getMovement().getLockDelay() > Utils.currentTimeMillis())
				return;
		}
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
				if ((hitpoints < player.getMaxHitpoints() * 0.1) && player.getPrayer().usingPrayer(0, 23)) {
					setNextGraphics(new Graphics(436));
					setHitpoints((int) (hitpoints + player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5));
					player.getSkills().set(Skills.PRAYER, 0);
					player.getPrayer().setPrayerpoints((byte) 0);
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

	public abstract void sendDeath(Entity source);

	public void processMovement() {
		boolean run = toPlayer().getMovement().isRun();
		lastWorldTile = new WorldTile(this);
		if (lastFaceEntity >= 0) {
			Entity target = lastFaceEntity >= 32768 ? World.getPlayers().get(lastFaceEntity - 32768) : World.getNPCs().get(lastFaceEntity);
			// was > if (target != null)
			if (target != null && (Utils.getDistance(this, target) < (this instanceof NPC ? 2 : 14)))
				direction = Utils.getFaceDirection(target.getCoordFaceX(target.getSize()) - getX(), target.getCoordFaceY(target.getSize()) - getY());
			else if (this instanceof NPC/* && !isStunned() */ /* !isStunned() because of Hans' running away */
					&& !((NPC) this).isUnderCombat()) /* under combat because if in combat, always nextface entity */
				setNextFaceEntity(null);
		}
		nextWalkDirection = nextRunDirection = -1;
		if (nextWorldTile != null) {
			int lastPlane = getPlane();
			setLocation(nextWorldTile);
			nextWorldTile = null;
			teleported = true;
			if (this instanceof Player && ((Player) this).getTemporaryMovementType() == -1)
				((Player) this).setTemporaryMovementType(toPlayer().getMovement().TELE_MOVE_TYPE);
			updateEntityRegion(this);
			if (needMapUpdate())
				loadMapRegions();
			else if (this instanceof Player && lastPlane != getPlane())
				((Player) this).setClientLoadedMapRegion(true);
			resetWalkSteps();
			return;
		}
		teleported = false;
		for (int stepCount = 0; stepCount < (run ? 2 : 1); stepCount++) {
			Object[] nextStep = getNextWalkStep();
			if (nextStep == null) {
				if (stepCount == 1 && this instanceof Player)
					((Player) this).setTemporaryMovementType(toPlayer().getMovement().WALK_MOVE_TYPE);
				break;
			}
			int dir = (int) nextStep[0];
			if (((boolean) nextStep[3] && !World.checkWalkStep(getPlane(), getX(), getY(), dir, getSize())) || (this instanceof Player && !((Player) this).getControlerManager().canMove(dir))) {
				resetWalkSteps();
				break;
			}
			if (stepCount == 0) {
				nextWalkDirection = dir;
			} else {
				nextRunDirection = dir;
				if (this instanceof Player) {
					if (!((Player) this).getControlerManager().canMove(
							nextWalkDirection)) {
						nextWalkDirection = -1;
						resetWalkSteps();
						return;
					}
					((Player) this).getMovement().drainRunEnergy();
				}
			}
			moveLocation(Utils.DIRECTION_DELTA_X[dir], Utils.DIRECTION_DELTA_Y[dir], 0);
			/*
			 * if (this instanceof Player) { Area area = AreaManager.get(this); if (area != null) { AreaManager.update((Player) this, area); } }
			 */
		}
		updateEntityRegion(this);
		if (needMapUpdate())
			loadMapRegions();
	}

	@Override
	public void moveLocation(int xOffset, int yOffset, int planeOffset) {
		super.moveLocation(xOffset, yOffset, planeOffset);
		direction = Utils.getFaceDirection(xOffset, yOffset);
	}

	private boolean needMapUpdate() {
		int lastMapRegionX = lastLoadedMapRegionTile.getChunkX();
		int lastMapRegionY = lastLoadedMapRegionTile.getChunkY();
		int regionX = getChunkX();
		int regionY = getChunkY();
		int size = ((Settings.MAP_SIZES[mapSize] >> 3) / 2) - 1;
		return Math.abs(lastMapRegionX - regionX) >= size || Math.abs(lastMapRegionY - regionY) >= size;
	}

	public boolean addWalkSteps(int destX, int destY) {
		return addWalkSteps(destX, destY, -1);
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
	 * return added all steps
	 */
	public boolean checkWalkStepsInteract(int fromX, int fromY, final int destX, final int destY, int maxStepsCount, int size, boolean calculate) {
		int[] lastTile = new int[] { fromX, fromY };
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		while (true) {
			stepCount++;
			int myRealX = myX;
			int myRealY = myY;

			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			if (!checkWalkStep(myX, myY, lastTile[0], lastTile[1], true)) {
				if (!calculate)
					return false;
				myX = myRealX;
				myY = myRealY;
				int[] myT = checkcalculatedStep(myRealX, myRealY, destX, destY, lastTile[0], lastTile[1], size);
				if (myT == null)
					return false;
				myX = myT[0];
				myY = myT[1];
			}
			int distanceX = myX - destX;
			int distanceY = myY - destY;
			if (!(distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1))
				return true;
			if (stepCount == maxStepsCount)
				return true;
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY)
				return true;
		}
	}

	public int[] checkcalculatedStep(int myX, int myY, int destX, int destY, int lastX, int lastY, int size) {
		if (myX < destX) {
			myX++;
			if (!checkWalkStep(myX, myY, lastX, lastY, true))
				myX--;
			else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		} else if (myX > destX) {
			myX--;
			if (!checkWalkStep(myX, myY, lastX, lastY, true))
				myX++;
			else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		}
		if (myY < destY) {
			myY++;
			if (!checkWalkStep(myX, myY, lastX, lastY, true))
				myY--;
			else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		} else if (myY > destY) {
			myY--;
			if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
				myY++;
			} else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		}
		if (myX == lastX || myY == lastY)
			return null;
		return new int[] { myX, myY };
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
			int dir = Utils.getMoveDirection(myX - lastTileX, myY - lastTileY);
			if (dir == -1)
				return false;
			if (checkClose) {
				if (!World.checkWalkStep(getPlane(), lastTileX, lastTileY, dir, size))
					return false;
			} else if (!World.checkProjectileStep(getPlane(), lastTileX, lastTileY, dir, size))
				return false;
			lastTileX = myX;
			lastTileY = myY;
			if (lastTileX == destX && lastTileY == destY)
				return true;
		}
	}

	public boolean addWalkStepsInteract(int destX, int destY, int maxStepsCount, int size, boolean calculate) {
		return addWalkStepsInteract(destX, destY, maxStepsCount, size, size, calculate);
	}

	public boolean canWalkNPC(int toX, int toY) {
		return canWalkNPC(toX, toY, false);
	}

	private int getPreviewNextWalkStep() {
		Object[] step = getMovement().getWalkSteps().poll();
		if (step == null)
			return -1;
		return (int) step[0];
	}

	public boolean canWalkNPC(int toX, int toY, boolean checkUnder) {
		if (!isAtMultiArea() /* || (!checkUnder && !canWalkNPC(getX(), getY(), true)) */)
			return true;
		int size = getSize();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
			if (npcIndexes != null) {
				for (int npcIndex : npcIndexes) {
					NPC target = World.getNPCs().get(npcIndex);
					if (target == null || target == this || target.isDead() || target.hasFinished() || target.getPlane() != getPlane() || !target.isAtMultiArea())
						continue;
					int targetSize = target.getSize();
					if (!checkUnder && target.getNextWalkDirection() == -1) { // means the walk hasnt been processed yet
						int previewDir = getPreviewNextWalkStep();
						if (previewDir != -1) {
							WorldTile tile = target.transform(Utils.DIRECTION_DELTA_X[previewDir], Utils.DIRECTION_DELTA_Y[previewDir], 0);
							if (colides(tile.getX(), tile.getY(), targetSize, getX(), getY(), size))
								continue;

							if (colides(tile.getX(), tile.getY(), targetSize, toX, toY, size))
								return false;
						}
					}
					if (colides(target.getX(), target.getY(), targetSize, getX(), getY(), size))
						continue;
					if (colides(target.getX(), target.getY(), targetSize, toX, toY, size))
						return false;
				}
			}
		}
		return true;
	}

	private static boolean colides(int x1, int y1, int size1, int x2, int y2, int size2) {
		for (int checkX1 = x1; checkX1 < x1 + size1; checkX1++) {
			for (int checkY1 = y1; checkY1 < y1 + size1; checkY1++) {
				for (int checkX2 = x2; checkX2 < x2 + size2; checkX2++) {
					for (int checkY2 = y2; checkY2 < y2 + size2; checkY2++) {
						if (checkX1 == checkX2 && checkY1 == checkY2)
							return true;
					}

				}
			}
		}
		return false;
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkStepsInteract(final int destX, final int destY, int maxStepsCount, int sizeX, int sizeY, boolean calculate) {
		int[] lastTile = getLastWalkTile();
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		while (true) {
			stepCount++;
			int myRealX = myX;
			int myRealY = myY;

			if (myX < destX)
				myX++;
			else if (myX > destX)
				myX--;
			if (myY < destY)
				myY++;
			else if (myY > destY)
				myY--;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastTile[0], lastTile[1], true)) {
				if (!calculate)
					return false;
				myX = myRealX;
				myY = myRealY;
				int[] myT = calculatedStep(myRealX, myRealY, destX, destY, lastTile[0], lastTile[1], sizeX, sizeY);
				if (myT == null)
					return false;
				myX = myT[0];
				myY = myT[1];
			}
			int distanceX = myX - destX;
			int distanceY = myY - destY;
			if (!(distanceX > sizeX || distanceX < -1 || distanceY > sizeY || distanceY < -1))
				return true;
			if (stepCount == maxStepsCount)
				return true;
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY)
				return true;
		}
	}

	public int[] calculatedStep(int myX, int myY, int destX, int destY, int lastX, int lastY, int sizeX, int sizeY) {
		if (myX < destX) {
			myX++;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true))
				myX--;
			else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		} else if (myX > destX) {
			myX--;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true))
				myX++;
			else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		}
		if (myY < destY) {
			myY++;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true))
				myY--;
			else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		} else if (myY > destY) {
			myY--;
			if ((this instanceof NPC && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
				myY++;
			} else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY)
					return null;
				return new int[] { myX, myY };
			}
		}
		if (myX == lastX || myY == lastY)
			return null;
		return new int[] { myX, myY };
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkSteps(final int destX, final int destY, int maxStepsCount) {
		return addWalkSteps(destX, destY, -1, true);
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkSteps(final int destX, final int destY, int maxStepsCount, boolean check) {
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
			if (!addWalkStep(myX, myY, lastTile[0], lastTile[1], check)) // cliped
																			// here
																			// so
																			// stop
				return false;
			if (stepCount == maxStepsCount)
				return true;
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY)
				return true;
		}
	}

	private int[] getLastWalkTile() {
		Object[] objects = getMovement().getWalkSteps().toArray();
		if (objects.length == 0)
			return new int[] { getX(), getY() };
		Object step[] = (Object[]) objects[objects.length - 1];
		return new int[] { (int) step[1], (int) step[2] };
	}

	// return cliped step
	public boolean checkWalkStep(int nextX, int nextY, int lastX, int lastY, boolean check) {
		int dir = Utils.getMoveDirection(nextX - lastX, nextY - lastY);
		if (dir == -1)
			return false;

		if (check && !World.checkWalkStep(getPlane(), lastX, lastY, dir, getSize())) {
			return false;
		}
		return true;
	}

	// return cliped step
	public boolean addWalkStep(int nextX, int nextY, int lastX, int lastY, boolean check) {
		int dir = Utils.getMoveDirection(nextX - lastX, nextY - lastY);
		if (dir == -1)
			return false;
		if (check && !World.checkWalkStep(getPlane(), lastX, lastY, dir, getSize()))// double check must be done sadly cuz of npc
																					// under check, can be improved later to
																					// only check when we want
			return false;
		if (this instanceof Player) {
			if (!((Player) this).getControlerManager().addWalkStep(lastX, lastY, nextX, nextY))
				return false;
		}
		getMovement().getWalkSteps().add(new Object[] { dir, nextX, nextY, check });
		return true;
	}

	public ConcurrentLinkedQueue<Object[]> getWalkSteps() {
		return getMovement().getWalkSteps();
	}

	public void resetWalkSteps() {
		getMovement().getWalkSteps().clear();
	}

	public boolean restoreHitPoints() {
		int maxHp = getMaxHitpoints();
		if (hitpoints > maxHp) {
			if (this instanceof Player) {
				Player player = (Player) this;
				if (player.getPrayer().usingPrayer(1, 5) && Utils.getRandom(100) <= 15)
					return false;
			}
			setHitpoints(hitpoints - 1);
			return true;
		} else if (hitpoints < maxHp) {
			setHitpoints(hitpoints + 1);
			if (this instanceof Player) {
				Player player = (Player) this;
				if (player.getPrayer().usingPrayer(0, 9) && hitpoints < maxHp)
					setHitpoints(hitpoints + 1);
				else if (player.getPrayer().usingPrayer(0, 26) && hitpoints < maxHp)
					setHitpoints(hitpoints + (hitpoints + 4 > maxHp ? maxHp - hitpoints : 4));

			}
			return true;
		}
		ifPlayer(p -> {
			if (isDead()) {
				return;
			}
			boolean update = restoreHitPoints();
			if (update) {
				if (p.getPrayer().usingPrayer(0, 9))
					restoreHitPoints();
				if (p.getMovement().isResting())
					restoreHitPoints();
				p.getSkills().refreshHitPoints();
			}
		});
		return false;
	}

	public boolean needMasksUpdate() {
		if (isPlayer())
			return (toPlayer().getTemporaryMovementType() != -1)
					|| (toPlayer().isUpdateMovementType()) || nextFaceEntity != -2 || nextAnimation != null || nextGraphics1 != null || nextGraphics2 != null
							|| nextGraphics3 != null || nextGraphics4 != null
							|| (nextWalkDirection == -1 && nextFaceWorldTile != null) || !nextHits.isEmpty()
							|| nextForceMovement != null || nextForceTalk != null;
		if (isNPC())
			return (toNPC().getNextTransformation() != null)|| nextFaceEntity != -2 || nextAnimation != null || nextGraphics1 != null || nextGraphics2 != null
					|| nextGraphics3 != null || nextGraphics4 != null
					|| (nextWalkDirection == -1 && nextFaceWorldTile != null) || !nextHits.isEmpty()
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
		if (nextWalkDirection == -1)
			nextFaceWorldTile = null;
		nextForceMovement = null;
		nextForceTalk = null;
		nextFaceEntity = -2;
		nextHits.clear();
		nextBars.clear();
		ifPlayer(player -> {
			player.setTemporaryMovementType((byte) -1);
			player.setUpdateMovementType(false);
			if (!player.isClientLoadedMapRegion()) {
				player.setClientLoadedMapRegion(true);
				World.getRegion(getRegionId()).refreshSpawnedObjects(player);
				World.getRegion(getRegionId()).refreshSpawnedItems(player);
			}
		});
		ifNpc(npc -> {
			npc.nextTransformation = null;
			npc.changedCombatLevel = false;
			npc.changedName = false;
		});
	}

	public abstract void finish();

	public int getMaxHitpoints() {
		return isNPC() ? toNPC().getCombatDefinitions().getHitpoints() : toPlayer().getSkills().getLevel(Skills.HITPOINTS) * 10 + toPlayer().getEquipment().getEquipmentHpIncrease();
	}

	public void processEntity() {
		processMovement();
		processReceivedHits();
		processReceivedDamage();
	}

	public void loadMapRegions() {
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
		lastLoadedMapRegionTile = new WorldTile(this); // creates a immutable
														// copy of this
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
			lastAnimationEnd = Utils.currentTimeMillis() + AnimationDefinitions.getAnimationDefinitions(nextAnimation.getIds()[0]).getEmoteTime();
		this.nextAnimation = nextAnimation;
	}

	public void setNextAnimationNoPriority(Animation nextAnimation) {
		if (lastAnimationEnd > Utils.currentTimeMillis())
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

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getDirection() {
		return direction;
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

	public int getNextWalkDirection() {
		return nextWalkDirection;
	}

	public int getNextRunDirection() {
		return nextRunDirection;
	}

	public void setRun(boolean run) {
		ifPlayer(p -> {
			if (run != p.getMovement().isRun()) {
				p.getMovement().setRun(run);
				p.setUpdateMovementType(true);
				p.getMovement().sendRunButtonConfig();
			}
		});
	}

	public WorldTile getNextFaceWorldTile() {
		return nextFaceWorldTile;
	}

	public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
		if (nextFaceWorldTile.getX() == getX() && nextFaceWorldTile.getY() == getY())
			return;
		this.nextFaceWorldTile = nextFaceWorldTile;
		if (nextWorldTile != null)
			direction = Utils.getFaceDirection(nextFaceWorldTile.getX() - nextWorldTile.getX(), nextFaceWorldTile.getY() - nextWorldTile.getY());
		else
			direction = Utils.getFaceDirection(nextFaceWorldTile.getX() - getX(), nextFaceWorldTile.getY() - getY());
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
		frozenBlocked = time + Utils.currentTimeMillis();
	}

	public void addFreezeDelay(long time) {
		addFreezeDelay(time, false);
	}

	public void addFreezeDelay(long time, boolean entangleMessage) {
		long currentTime = Utils.currentTimeMillis();
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

	private Object[] getNextWalkStep() {
		Object[] step = getMovement().getWalkSteps().poll();
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
				player.getControlerManager().moved();
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
			if (entity instanceof Player) {
				Player player = (Player) entity;
				player.getControlerManager().moved();
				if (player.isStarted()) {
//					checkControlersAtMove(player);					
				}
			}
			entity.checkMultiArea();
		}
	}
	
	/**
	 * Gets the attribute from the {@link #attributes} map, and if it doesn't exist, we return the default value
	 *
	 * @param key
	 * 		The key of the attribute
	 * @param defaultValue
	 * 		The default value
	 * @param <T>
	 * 		The return type
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Object key, T defaultValue) {
		T value = (T) attributes.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	/**
	 * The map of temporary attributes
	 */
	private transient ConcurrentHashMap<Object, Object> attributes;
	
	/**
	 * Puts the key into the attributes map
	 *
	 * @param key
	 * 		The key
	 * @param value
	 * 		The value
	 */
	public <T> T putAttribute(Object key, T value) {
		attributes.put(key, value);
		return value;
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
}