package com.jupiter.combat.npc;

import java.util.ArrayList;
import java.util.List;

import com.jupiter.cache.loaders.NPCDefinitions;
import com.jupiter.combat.npc.combat.NPCCombat;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.cores.CoresManager;
import com.jupiter.game.Entity;
import com.jupiter.game.EntityType;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.activity.impl.WildernessActivity;
import com.jupiter.game.route.ClipType;
import com.jupiter.game.route.Direction;
import com.jupiter.game.route.RouteFinder;
import com.jupiter.game.route.strategy.DumbRouteFinder;
import com.jupiter.game.route.strategy.FixedTileStrategy;
import com.jupiter.game.task.Task;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;
import com.jupiter.network.encoders.other.Hit;
import com.jupiter.network.encoders.other.Hit.HitLook;
import com.jupiter.utility.NPCBonuses;
import com.jupiter.utility.NPCCombatDefinitionsL;
import com.jupiter.utility.RandomUtility;
import com.jupiter.utility.Utility;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class NPC extends Entity {

	private int NORMAL_WALK = 0x2, WATER_WALK = 0x4, FLY_WALK = 0x8;

	private int id;
	private WorldTile respawnTile;
	private boolean canBeAttackFromOutOfArea;
	private boolean randomWalk;
	private int[] bonuses;
	private boolean spawned;
	private transient NPCCombat combat;
	private WorldTile forceWalk;
	private int walkType;

	private long lastAttackedByTarget;
	private boolean cantInteract;
	private int capDamage;
	private int lureDelay;
	private boolean cantFollowUnderCombat;
	private boolean forceAgressive;
	private int forceTargetDistance;
	private boolean forceFollowClose;
	private boolean forceMultiAttacked;
	private boolean noDistanceCheck;

	// npc masks
	private transient Transformation nextTransformation;
	private String name;
	private transient boolean changedName;
	private int combatLevel;
	private transient boolean changedCombatLevel;
	private transient boolean locked;

	public NPC(int id, WorldTile tile, boolean canBeAttackFromOutOfArea) {
		this(id, tile, canBeAttackFromOutOfArea, false);
	}
	
	public NPC(int id, WorldTile tile) {
		this(id, tile, false, false);
	}

	/*
	 * creates and adds npc
	 */
	public NPC(int id, WorldTile tile, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(tile, EntityType.NPC);
		this.id = id;
		this.respawnTile = new WorldTile(tile);
		this.canBeAttackFromOutOfArea = canBeAttackFromOutOfArea;
		this.setSpawned(spawned);
		combatLevel = -1;
		setHitpoints(getMaxHitpoints());
		setWalkType(getDefinitions().walkMask);
		setStartTile(tile);
		setClipType((getDefinitions().walkMask & 0x4) != 0 ? ClipType.WATER : ClipType.NORMAL);
		if (getName().contains("impling")) {
			setRandomWalk(true);
			setClipType(ClipType.FLYING);
		}
		direction = getRespawnDirection();
		bonuses = NPCBonuses.getBonuses(id);
		combat = new NPCCombat(this);
		capDamage = -1;
		lureDelay = 12000;
		initEntity();
		World.addNPC(this);
		updateEntityRegion(this);
		loadMapRegions();
		checkMultiArea();
	}

	public void transformIntoNPC(int id) {
		setNPC(id);
		nextTransformation = new Transformation(id);
	}

	public void setNPC(int id) {
		this.id = id;
		bonuses = NPCBonuses.getBonuses(id);
	}

	public NPCDefinitions getDefinitions() {
		return NPCDefinitions.getNPCDefinitions(id);
	}

	public NPCCombatDefinitions getCombatDefinitions() {
		return NPCCombatDefinitionsL.getNPCCombatDefinitions(id);
	}

	public void processNPC() {
		if (isDead() || locked)
			return;
		if (World.getPlayersInRegionRange(getRegionId()).isEmpty())
			return;
		if (!combat.process()) { // if not under combat
			if (!isForceWalking()) {// combat still processed for attack delay
				// go down
				// random walk
				if (!cantInteract) {
					if (!checkAgressivity()) {
						if (getFreezeDelay() < Utility.currentTimeMillis()) {
							if (!hasWalkSteps() && (walkType & NORMAL_WALK) != 0) {
								boolean can = Math.random() > 0.9;
								if (can) {
									int moveX = RandomUtility.random(getDefinitions().hasAttackOption() ? 4 : 2, getDefinitions().hasAttackOption() ? 8 : 4);
									int moveY = RandomUtility.random(getDefinitions().hasAttackOption() ? 4 : 2, getDefinitions().hasAttackOption() ? 8 : 4);
									if (RandomUtility.random(2) == 0)
										moveX = -moveX;
									if (RandomUtility.random(2) == 0)
										moveY = -moveY;
									resetWalkSteps();
									DumbRouteFinder.addDumbPathfinderSteps(this, respawnTile.transform(moveX, moveY, 0), getDefinitions().hasAttackOption() ? 7 : 3, getClipType());
									if (Utility.getDistance(this, respawnTile) > 3 && !getDefinitions().hasAttackOption()) {
										DumbRouteFinder.addDumbPathfinderSteps(this, respawnTile, getDefinitions().hasAttackOption() ? 7 : 3, getClipType());
									}
								}

							}
						}
					}
				}
			}
		}
		if (isForceWalking()) {
			if (getFreezeDelay() < Utility.currentTimeMillis()) {
				if (getX() != forceWalk.getX() || getY() != forceWalk.getY()) {
					if (!hasWalkSteps()) {
						int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), new FixedTileStrategy(forceWalk.getX(), forceWalk.getY()), true);
						int[] bufferX = RouteFinder.getLastPathBufferX();
						int[] bufferY = RouteFinder.getLastPathBufferY();
						for (int i = steps - 1; i >= 0; i--) {
							if (!addWalkSteps(bufferX[i], bufferY[i], 25, true, true))
								break;
						}
					}
					if (!hasWalkSteps()) {
						setNextWorldTile(new WorldTile(forceWalk));
						forceWalk = null;
					}
				} else
					forceWalk = null;
			}
		}
	}

	public int getRespawnDirection() {
		return Direction.getById(getDefinitions().respawnDirection).getAngle();
	}

	/*
	 * forces npc to random walk even if cache says no, used because of fake cache information
	 */
	@SuppressWarnings("unused")
	private static boolean forceRandomWalk(int npcId) {
		switch (npcId) {
		case 11226:
			return true;
		case 3341:
		case 3342:
		case 3343:
			return true;
		default:
			return false;
		/*
		 * default: return NPCDefinitions.getNPCDefinitions(npcId).name .equals("Icy Bones");
		 */
		}
	}

	public void sendSoulSplit(final Hit hit, final Entity user) {
		final NPC target = this;
		if (hit.getDamage() > 0)
			World.sendProjectile(user, this, 2263, 11, 11, 20, 5, 0, 0);
		user.heal(hit.getDamage() / 5);
		World.get().submit(new Task(1) {
			@Override
			protected void execute() {
				setNextGraphics(new Graphics(2264));
				if (hit.getDamage() > 0)
					World.sendProjectile(target, user, 2263, 11, 11, 20, 5, 0, 0);
			}
		});
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (capDamage != -1 && hit.getDamage() > capDamage)
			hit.setDamage(capDamage);
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE)
			return;
		Entity source = hit.getSource();
		if (source == null)
			return;
		if (source instanceof Player) {
			final Player p2 = (Player) source;
			if (p2.getPrayer().hasPrayersOn()) {
				if (p2.getPrayer().usingPrayer(1, 18))
					sendSoulSplit(hit, p2);
				if (hit.getDamage() == 0)
					return;
				if (!p2.getPrayer().isBoostedLeech()) {
					if (hit.getLook() == HitLook.MELEE_DAMAGE) {
						if (p2.getPrayer().usingPrayer(1, 19)) {
							p2.getPrayer().setBoostedLeech(true);
							return;
						} else if (p2.getPrayer().usingPrayer(1, 1)) { // sap
							// att
							if (RandomUtility.random(4) == 0) {
								if (p2.getPrayer().reachedMax(0)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(0);
									p2.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
								}
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2214));
								p2.getPrayer().setBoostedLeech(true);
								World.sendProjectile(p2, this, 2215, 35, 35, 20, 5, 0, 0);
								World.get().submit(new Task(1) {
									@Override
									protected void execute() {
										setNextGraphics(new Graphics(2216));
										this.cancel();
									}
								});
								return;
							}
						} else {
							if (p2.getPrayer().usingPrayer(1, 10)) {
								if (RandomUtility.random(7) == 0) {
									if (p2.getPrayer().reachedMax(3)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
									} else {
										p2.getPrayer().increaseLeechBonus(3);
										p2.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
									}
									p2.setNextAnimation(new Animation(12575));
									p2.getPrayer().setBoostedLeech(true);
									World.sendProjectile(p2, this, 2231, 35, 35, 20, 5, 0, 0);
									World.get().submit(new Task(1) {
										@Override
										protected void execute() {
											setNextGraphics(new Graphics(2232));
											this.cancel();
										}
									});
									return;
								}
							}
							if (p2.getPrayer().usingPrayer(1, 14)) {
								if (RandomUtility.random(7) == 0) {
									if (p2.getPrayer().reachedMax(7)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
									} else {
										p2.getPrayer().increaseLeechBonus(7);
										p2.getPackets().sendGameMessage("Your curse drains Strength from the enemy, boosting your Strength.", true);
									}
									p2.setNextAnimation(new Animation(12575));
									p2.getPrayer().setBoostedLeech(true);
									World.sendProjectile(p2, this, 2248, 35, 35, 20, 5, 0, 0);
									World.get().submit(new Task(1) {
										@Override
										protected void execute() {
											setNextGraphics(new Graphics(2250));
											this.cancel();
										}
									});
									return;
								}
							}

						}
					}
					if (hit.getLook() == HitLook.RANGE_DAMAGE) {
						if (p2.getPrayer().usingPrayer(1, 2)) { // sap range
							if (RandomUtility.random(4) == 0) {
								if (p2.getPrayer().reachedMax(1)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(1);
									p2.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
								}
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2217));
								p2.getPrayer().setBoostedLeech(true);
								World.sendProjectile(p2, this, 2218, 35, 35, 20, 5, 0, 0);
								World.get().submit(new Task(1) {
									@Override
									protected void execute() {
										setNextGraphics(new Graphics(2219));
										this.cancel();
									}
								});
								return;
							}
						} else if (p2.getPrayer().usingPrayer(1, 11)) {
							if (RandomUtility.random(7) == 0) {
								if (p2.getPrayer().reachedMax(4)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(4);
									p2.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
								}
								p2.setNextAnimation(new Animation(12575));
								p2.getPrayer().setBoostedLeech(true);
								World.sendProjectile(p2, this, 2236, 35, 35, 20, 5, 0, 0);
								World.get().submit(new Task(1) {
									@Override
									protected void execute() {
										setNextGraphics(new Graphics(2238));
										this.cancel();
									}
								});
								return;
							}
						}
					}
					if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
						if (p2.getPrayer().usingPrayer(1, 3)) { // sap mage
							if (RandomUtility.random(4) == 0) {
								if (p2.getPrayer().reachedMax(2)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(2);
									p2.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
								}
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2220));
								p2.getPrayer().setBoostedLeech(true);
								World.sendProjectile(p2, this, 2221, 35, 35, 20, 5, 0, 0);
								World.get().submit(new Task(1) {
									@Override
									protected void execute() {
										setNextGraphics(new Graphics(2222));
										this.cancel();
									}
								});
								return;
							}
						} else if (p2.getPrayer().usingPrayer(1, 12)) {
							if (RandomUtility.random(7) == 0) {
								if (p2.getPrayer().reachedMax(5)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(5);
									p2.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
								}
								p2.setNextAnimation(new Animation(12575));
								p2.getPrayer().setBoostedLeech(true);
								World.sendProjectile(p2, this, 2240, 35, 35, 20, 5, 0, 0);
								World.get().submit(new Task(1) {
									@Override
									protected void execute() {
										setNextGraphics(new Graphics(2242));
										this.cancel();
									}
								});
								return;
							}
						}
					}

					// overall

					if (p2.getPrayer().usingPrayer(1, 13)) { // leech defence
						if (RandomUtility.random(10) == 0) {
							if (p2.getPrayer().reachedMax(6)) {
								p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
							} else {
								p2.getPrayer().increaseLeechBonus(6);
								p2.getPackets().sendGameMessage("Your curse drains Defence from the enemy, boosting your Defence.", true);
							}
							p2.setNextAnimation(new Animation(12575));
							p2.getPrayer().setBoostedLeech(true);
							World.sendProjectile(p2, this, 2244, 35, 35, 20, 5, 0, 0);
							World.get().submit(new Task(1) {
								@Override
								protected void execute() {
									setNextGraphics(new Graphics(2246));
									this.cancel();
								}
							});
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void finish() {
		if (hasFinished())
			return;
		setFinished(true);
		updateEntityRegion(this);
		World.removeNPC(this);
	}

	public void setRespawnTask() {
		setRespawnTask(-1);
	}
	
	public void setRespawnTask(int time) {
		if (!hasFinished()) {
			reset();
			setLocation(respawnTile);
			finish();
		}
		CoresManager.schedule(() -> spawn(), time < 0 ? getCombatDefinitions().getRespawnDelay() : time);
	}
	
	public void deserialize() {
		if (combat == null)
			combat = new NPCCombat(this);
		spawn();
	}

	public void spawn() {
		setFinished(false);
		World.addNPC(this);
		setLastRegionId(0);
		updateEntityRegion(this);
		loadMapRegions();
		checkMultiArea();
	}

	public void drop() {
		Player killer = getMostDamageReceivedSourcePlayer();
		if (killer == null)
			return;
		//do stuff that doesn't exist yet
	}

	public int getMaxHit() {
		return getCombatDefinitions().getMaxHit();
	}

	public boolean isUnderCombat() {
		return combat.underCombat();
	}

	@Override
	public void setAttackedBy(Entity target) {
		super.setAttackedBy(target);
		if (target == combat.getTarget())
			lastAttackedByTarget = Utility.currentTimeMillis();
	}

	public boolean canBeAttackedByAutoRelatie() {
		return Utility.currentTimeMillis() - lastAttackedByTarget > lureDelay;
	}

	public boolean isForceWalking() {
		return forceWalk != null;
	}

	public void setTarget(Entity entity) {
		if (isForceWalking()) // if force walk not gonna get target
			return;
		combat.setTarget(entity);
		lastAttackedByTarget = Utility.currentTimeMillis();
	}

	public void removeTarget() {
		if (combat.getTarget() == null)
			return;
		combat.removeTarget();
	}

	public void forceWalkRespawnTile() {
		setForceWalk(respawnTile);
	}

	public void setForceWalk(WorldTile tile) {
		resetWalkSteps();
		forceWalk = tile;
	}

	public boolean hasForceWalk() {
		return forceWalk != null;
	}

	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int playerIndex : playerIndexes) {
					Player player = World.getPlayers().get(playerIndex);
					if (player == null || player.isDead() || player.hasFinished() || !player.isActive() || !player.withinDistance(this, forceTargetDistance > 0 ? forceTargetDistance : (getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.MELEE ? 4 : getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.SPECIAL ? 64 : 8)) || (!forceMultiAttacked && (!isAtMultiArea() || !player.isAtMultiArea()) && player.getAttackedBy() != this && (player.getAttackedByDelay() > Utility.currentTimeMillis() || player.getFindTargetDelay() > Utility.currentTimeMillis())) || !clipedProjectile(player, false) || (!forceAgressive && !WildernessActivity.isAtWild(this) && player.getSkills().getCombatLevelWithSummoning() >= getCombatLevel() * 2))
						continue;
					possibleTarget.add(player);
				}
			}
		}
		return possibleTarget;
	}

	public boolean checkAgressivity() {
		// if(!(Wilderness.isAtWild(this) &&
		// getDefinitions().hasAttackOption())) {
		if (!forceAgressive) {
			NPCCombatDefinitions defs = getCombatDefinitions();
			if (defs.getAgressivenessType() == NPCCombatDefinitions.PASSIVE)
				return false;
		}
		// }
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			Entity target = possibleTarget.get(RandomUtility.random(possibleTarget.size()));
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utility.currentTimeMillis() + 10000);
			return true;
		}
		return false;
	}

	public void setCantInteract(boolean cantInteract) {
		this.cantInteract = cantInteract;
		if (cantInteract)
			combat.reset();
	}

	@Override
	public String toString() {
		return getDefinitions().name + " - " + id + " - " + getX() + " " + getY() + " " + getPlane();
	}

	public String getCustomName() {
		return name;
	}

	public void setName(String string) {
		this.name = getDefinitions().name.equals(string) ? null : string;
		changedName = true;
	}

	public int getCustomCombatLevel() {
		return combatLevel;
	}

	public int getCombatLevel() {
		return combatLevel >= 0 ? combatLevel : getDefinitions().combatLevel;
	}

	public String getName() {
		return name != null ? name : getDefinitions().name;
	}

	public void setCombatLevel(int level) {
		combatLevel = getDefinitions().combatLevel == level ? -1 : level;
		changedCombatLevel = true;
	}

	public WorldTile getMiddleWorldTile() {
		int size = getSize();
		return new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane());
	}

	public boolean withinDistance(Player tile, int distance) {
		return super.withinDistance(tile, distance);
	}
	
	private WorldTile startTile;
	
	//TODO: Rework how npc instances are done. No more of this big code block stuff.
	public static final NPC spawnNPC(int id, WorldTile tile, boolean canBeAttackFromOutOfArea, boolean spawned) {
		return new NPC(id, tile,canBeAttackFromOutOfArea, spawned);
	}

	public static final NPC spawnNPC(int id, WorldTile tile, boolean canBeAttackFromOutOfArea) {
		return spawnNPC(id, tile, canBeAttackFromOutOfArea, false);
	}
}