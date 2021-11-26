package com.jupiter.game.player;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.jupiter.Settings;
import com.jupiter.cache.loaders.VarManager;
import com.jupiter.combat.player.CombatDefinitions;
import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.combat.player.type.AntifireDetails;
import com.jupiter.combat.player.type.CombatEffect;
import com.jupiter.cores.CoresManager;
import com.jupiter.game.Entity;
import com.jupiter.game.EntityType;
import com.jupiter.game.dialogue.Conversation;
import com.jupiter.game.dialogue.Dialogue;
import com.jupiter.game.item.FloorItem;
import com.jupiter.game.item.Item;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.InterfaceManager.Tab;
import com.jupiter.game.player.actions.ActionManager;
import com.jupiter.game.player.content.AuraManager;
import com.jupiter.game.player.content.Emotes;
import com.jupiter.game.player.content.FriendChatsManager;
import com.jupiter.game.player.content.LodeStone;
import com.jupiter.game.player.content.MusicsManager;
import com.jupiter.game.player.content.PriceCheckManager;
import com.jupiter.game.player.content.Toolbelt;
import com.jupiter.game.player.controlers.ControlerManager;
import com.jupiter.game.player.controlers.Wilderness;
import com.jupiter.game.route.CoordsEvent;
import com.jupiter.game.route.strategy.RouteEvent;
import com.jupiter.game.task.Task;
import com.jupiter.game.task.impl.CombatEffectTask;
import com.jupiter.game.task.impl.SkillActionTask;
import com.jupiter.net.Session;
import com.jupiter.net.decoders.LogicPacket;
import com.jupiter.net.decoders.WorldPacketsDecoder;
import com.jupiter.net.encoders.WorldPacketsEncoder;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.encoders.other.Graphics;
import com.jupiter.net.encoders.other.HintIconsManager;
import com.jupiter.net.encoders.other.Hit;
import com.jupiter.net.encoders.other.LocalNPCUpdate;
import com.jupiter.net.encoders.other.LocalPlayerUpdate;
import com.jupiter.net.encoders.other.PublicChatMessage;
import com.jupiter.net.host.HostListType;
import com.jupiter.net.host.HostManager;
import com.jupiter.skills.Skills;
import com.jupiter.skills.prayer.Prayer;
import com.jupiter.utils.IsaacKeyPair;
import com.jupiter.utils.Logger;
import com.jupiter.utils.MutableNumber;
import com.jupiter.utils.Utils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false) // what plugin is this
public class Player extends Entity {

	public static final byte TELE_MOVE_TYPE = 127, WALK_MOVE_TYPE = 1, RUN_MOVE_TYPE = 2;

	// transient stuff
	private transient String username;
	private transient Session session;
	private transient boolean clientLoadedMapRegion;
	private transient byte displayMode;
	private transient short screenWidth;
	private transient short screenHeight;
	private transient InterfaceManager interfaceManager;
	private transient HintIconsManager hintIconsManager;
	private transient ActionManager actionManager;
	private transient PriceCheckManager priceCheckManager;
	private transient CoordsEvent coordsEvent;
	private transient FriendChatsManager currentFriendChat;
	private transient Trade trade;
	private transient IsaacKeyPair isaacKeyPair;

	//Stones
	private transient boolean[] activatedLodestones;
	private transient LodeStone lodeStone;

	//Pins
	public transient String lastIPBankWasOpened;
	public transient boolean bypass;

	// used for packets logic
	private transient ConcurrentLinkedQueue<LogicPacket> logicPackets;

	// used for update
	private transient LocalPlayerUpdate localPlayerUpdate;
	private transient LocalNPCUpdate localNPCUpdate;

	private transient byte temporaryMovementType;
	private transient boolean updateMovementType;

	// player stages - not personal
	private transient boolean started;
	private transient boolean isActive;

	private transient long packetsDecoderPing;
	private transient boolean resting;
	private transient boolean canPvp;
	private transient boolean cantTrade;
	private transient long lockDelay; // used for doors and stuff like that
	private transient Runnable closeInterfacesEvent;
	private transient long lastPublicMessage;
	private transient List<Byte> switchItemCache;
	private transient boolean disableEquip;
	private transient boolean castedVeng;
	private transient double hpBoostMultiplier;
	private transient boolean largeSceneView;
	private transient RouteEvent routeEvent;
	
	private transient Conversation conversation;
	
	/**
	 * Represents a Player's last Emote delay (used for various things)
	 */
	private transient long nextEmoteEnd;
	
	/**
	 * Creates a new instance of a Players details
	 */
	public PlayerDetails playerDetails = new PlayerDetails();
	
	private Appearance appearence;
	private Inventory inventory;
	private Equipment equipment;
	private Skills skills;
	private CombatDefinitions combatDefinitions;
	private Prayer prayer;
	private Bank bank;
	private ControlerManager controlerManager;
	private MusicsManager musicsManager;
	private FriendsIgnores friendsIgnores;
	private AuraManager auraManager;
	
	public transient VarManager varsManager;
	
	private transient boolean forceNextMapLoadRefresh;

	

	// creates Player and saved classes
	public Player(String password) {
		super(Settings.START_PLAYER_LOCATION, EntityType.PLAYER);
		setHitpoints(100);
		playerDetails = new PlayerDetails();
		getPlayerDetails().setPassword(password);
		this.toolbelt = new Toolbelt();
		varsManager = new VarManager(this);
		appearence = new Appearance();
		inventory = new Inventory();
		equipment = new Equipment();
		skills = new Skills();
		combatDefinitions = new CombatDefinitions();
		prayer = new Prayer();
		bank = new Bank();
		controlerManager = new ControlerManager();
		musicsManager = new MusicsManager();
		friendsIgnores = new FriendsIgnores();
		auraManager = new AuraManager();
		lodeStone = new LodeStone();
	}

	public void init(Session session, String username, byte displayMode, short screenWidth, short screenHeight, IsaacKeyPair isaacKeyPair) {
		// temporary deleted after reset all chars
		this.session = session;
		this.username = username;
		this.displayMode = displayMode;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.isaacKeyPair = isaacKeyPair;
		if (playerDetails == null)
			playerDetails = new PlayerDetails();
		if (auraManager == null)
			auraManager = new AuraManager();
		if(toolbelt == null)
			this.toolbelt = new Toolbelt();
		if (lodeStone == null)
			lodeStone = new LodeStone();
		if (activatedLodestones == null)
			activatedLodestones = new boolean[16];
		if (varsManager == null)
			varsManager = new VarManager(this);
		interfaceManager = new InterfaceManager(this);
		hintIconsManager = new HintIconsManager(this);
		priceCheckManager = new PriceCheckManager(this);
		localPlayerUpdate = new LocalPlayerUpdate(this);
		localNPCUpdate = new LocalNPCUpdate(this);
		actionManager = new ActionManager(this);
		trade = new Trade(this);
		lodeStone.setPlayer(this);
		// loads player on saved instances
		appearence.setPlayer(this);
		inventory.setPlayer(this);
		equipment.setPlayer(this);
		skills.setPlayer(this);
		toolbelt.setPlayer(this);
		combatDefinitions.setPlayer(this);
		prayer.setPlayer(this);
		bank.setPlayer(this);
		controlerManager.setPlayer(this);
		musicsManager.setPlayer(this);
		friendsIgnores.setPlayer(this);
		auraManager.setPlayer(this);
		temporaryMovementType = -1;
		logicPackets = new ConcurrentLinkedQueue<LogicPacket>();
		switchItemCache = Collections.synchronizedList(new ArrayList<Byte>());
		initEntity();
		packetsDecoderPing = Utils.currentTimeMillis();
		World.addPlayer(this);
		updateEntityRegion(this);

		// Do not delete >.>, useful for security purpose. this wont waste that much space..
		if (getPlayerDetails().getPasswordList() == null)
			getPlayerDetails().setPasswordList(new ArrayList<String>());
		if (getPlayerDetails().getIpList() == null)
			getPlayerDetails().setIpList(new ArrayList<String>());
		updateIPnPass();
	}
	
	public boolean hasSkull() {
		return getSkullTimer().get() > 0;
	}

	public void refreshSpawnedItems() {
		for (int regionId : getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getFloorItems();
			if (floorItems == null)
				continue;
			for (FloorItem item : floorItems) {
				if ((item.isInvisible() || item.isGrave()) && this != item.getOwner()
						|| item.getTile().getPlane() != getPlane())
					continue;
				getPackets().sendRemoveGroundItem(item);
			}
		}
		for (int regionId : getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getFloorItems();
			if (floorItems == null)
				continue;
			for (FloorItem item : floorItems) {
				if ((item.isInvisible() || item.isGrave()) && this != item.getOwner()
						|| item.getTile().getPlane() != getPlane())
					continue;
				getPackets().sendGroundItem(item);
			}
		}
	}

	public void refreshSpawnedObjects() {
		for (int regionId : getMapRegionsIds()) {
			List<WorldObject> spawnedObjects = World.getRegion(regionId).getSpawnedObjects();
			if (spawnedObjects != null) {
				for (WorldObject object : spawnedObjects)
					if (object.getPlane() == getPlane())
						getPackets().sendSpawnedObject(object);
			}
			List<WorldObject> removedObjects = World.getRegion(regionId).getRemovedObjects();
			if (removedObjects != null) {
				for (WorldObject object : removedObjects)
					if (object.getPlane() == getPlane())
						getPackets().sendDestroyObject(object);
			}
		}
	}

	// now that we inited we can start showing game
	public void start() {
		loadMapRegions();
		started = true;
		run();

		if (isDead())
			sendDeath(null);
	}

	public void stopAll() {
		stopAll(true);
	}

	public void stopAll(boolean stopWalk) {
		stopAll(stopWalk, true);
	}

	public void stopAll(boolean stopWalk, boolean stopInterface) {
		stopAll(stopWalk, stopInterface, true);
	}

	// as walk done clientsided - not anymore buddy
	public void stopAll(boolean stopWalk, boolean stopInterfaces, boolean stopActions) {
		if (stopInterfaces)
			closeInterfaces();
		if (stopWalk){
			coordsEvent = null;
			routeEvent = null;
			resetWalkSteps();
			getPackets().sendResetMinimapFlag();
		}
		if (stopActions)
			actionManager.forceStop();
		combatDefinitions.resetSpells(false);
	}

	@Override
	public void reset(boolean attributes) {
		super.reset(attributes);
		refreshHitPoints();
		hintIconsManager.removeAll();
		skills.restoreSkills();
		combatDefinitions.resetSpecialAttack();
		prayer.reset();
		combatDefinitions.resetSpells(true);
		resting = false;
		getPoisonDamage().set(0);
		castedVeng = false;
		setRunEnergy(100);
		appearence.getAppeareanceBlocks();
	}

	@Override
	public void reset() {
		reset(true);
	}

	public void closeInterfaces() {
		if (interfaceManager.containsScreenInter())
			interfaceManager.closeScreenInterface();
		if (interfaceManager.containsInventoryInter())
			interfaceManager.closeInventoryInterface();
		
		endConversation();
		
		getInterfaceManager().closeChatBoxInterface();
		getTemporaryAttributtes().remove("dialogue_event");
		
		if (closeInterfacesEvent != null) {
			closeInterfacesEvent.run();
			closeInterfacesEvent = null;
		}
	}
	
	public void startConversation(Dialogue dialogue) {
		startConversation(new Conversation(dialogue.finish()));
	}

	public boolean startConversation(Conversation conversation) {
		if (conversation.getCurrent() == null)
			return false;
		this.conversation = conversation;
		this.conversation.setPlayer(this);
		conversation.start();
		return true;
	}

	public void endConversation() {
		this.conversation = null;
		if (getInterfaceManager().containsChatBoxInter())
			getInterfaceManager().closeChatBoxInterface();
	}
	
	public void setClientHasntLoadedMapRegion() {
		clientLoadedMapRegion = false;
	}

	@Override
	public void loadMapRegions() {
		boolean wasAtDynamicRegion = isAtDynamicRegion();
		super.loadMapRegions();
		clientLoadedMapRegion = false;
		if (isAtDynamicRegion()) {
			getPackets().sendDynamicMapRegion(!started);
			if (!wasAtDynamicRegion)
				localNPCUpdate.reset();
		} else {
			getPackets().sendMapRegion(!started);
			if (wasAtDynamicRegion)
				localNPCUpdate.reset();
		}
		forceNextMapLoadRefresh = false;
	}

	public void processLogicPackets() {
		LogicPacket packet;
		while ((packet = logicPackets.poll()) != null)
			WorldPacketsDecoder.decodeLogicPacket(this, packet);
	}
	
	@Override
	public void processEntity() {
		if (isDead() || !isActive()) {
			return;
		}
		if (finishing)
			finish(0);
		processLogicPackets();
		super.processEntity();
		if (coordsEvent != null && coordsEvent.processEvent(this))
			coordsEvent = null;
		if (routeEvent != null && routeEvent.processEvent(this))
			routeEvent = null;
		
		if (musicsManager.musicEnded())
			musicsManager.replayMusic();
		
		if (!(getControlerManager().getControler() instanceof Wilderness) && isAtWild()
				&& !Wilderness.isAtWildSafe(this)) {
			getControlerManager().startControler("Wilderness");
		}

		auraManager.process();
		actionManager.process();
		controlerManager.process();
	}
	
	public final boolean isAtWild() {
		return (getX() >= 3011 && getX() <= 3132 && getY() >= 10052 && getY() <= 10175)
				|| (getX() >= 2940 && getX() <= 3395 && getY() >= 3525 && getY() <= 4000)
				|| (getX() >= 3264 && getX() <= 3279 && getY() >= 3279 && getY() <= 3672)
				|| (getX() >= 3158 && getX() <= 3181 && getY() >= 3679 && getY() <= 3697)
				|| (getX() >= 3280 && getX() <= 3183 && getY() >= 3885 && getY() <= 3888)
				|| (getX() >= 3012 && getX() <= 3059 && getY() >= 10303 && getY() <= 10351)
				|| (getX() >= 3060 && getX() <= 3072 && getY() >= 10251 && getY() <= 10263);
	}


	public void restoreSkills() {
        for (int skill = 0; skill < 25; skill++) {
            if (skill == Skills.HITPOINTS || skill == Skills.SUMMONING || skill == Skills.PRAYER)
                continue;
            int currentLevel = getSkills().getLevel(skill);
            int normalLevel = getSkills().getLevelForXp(skill);
            if (currentLevel < normalLevel) {
                getSkills().set(skill, currentLevel + 1);

            }
        }
    }
    
    public void drainSkills() {
        for (int skill = 0; skill < 25; skill++) {
            if (skill == Skills.HITPOINTS)
                continue;
            int currentLevel = getSkills().getLevel(skill);
            int normalLevel = getSkills().getLevelForXp(skill);
            if (currentLevel > normalLevel) {
                getSkills().set(skill, currentLevel - 1);

            }
        }
    }
    

	@Override
	public void processReceivedHits() {
		if (lockDelay > Utils.currentTimeMillis())
			return;
		super.processReceivedHits();
	}

	@Override
	public boolean needMasksUpdate() {
		return super.needMasksUpdate() || temporaryMovementType != -1 || updateMovementType;
	}

	@Override
	public void resetMasks() {
		super.resetMasks();
		temporaryMovementType = -1;
		updateMovementType = false;
		if (!clientHasLoadedMapRegion()) {
			// load objects and items here
			setClientHasLoadedMapRegion();
			refreshSpawnedObjects();
			refreshSpawnedItems();
		}
	}

	public void toogleRun(boolean update) {
		super.setRun(!getRun());
		updateMovementType = true;
		if (update)
			sendRunButtonConfig();
	}

	public void setRunHidden(boolean run) {
		super.setRun(run);
		updateMovementType = true;
	}

	@Override
	public void setRun(boolean run) {
		if (run != getRun()) {
			super.setRun(run);
			updateMovementType = true;
			sendRunButtonConfig();
		}
	}

	public void sendRunButtonConfig() {
		getPackets().sendConfig(173, resting ? 3 : getRun() ? 1 : 0);
	}

	public void run() {
		if (World.exiting_start != 0) {
			int delayPassed = (int) ((Utils.currentTimeMillis() - World.exiting_start) / 1000);
			getPackets().sendSystemUpdate(World.exiting_delay - delayPassed);
		}
		getAppearence().generateAppearenceData();
		getPlayerDetails().setLastIP(getSession().getIP());
		getInterfaceManager().sendInterfaces();
		getPackets().sendRunEnergy();
		getPackets().sendItemsLook();
		refreshAllowChatEffects();
		refreshMouseButtons();
		refreshPrivateChatSetup();
		refreshOtherChatsSetup();
		CombatEffect.values().forEach($it -> {
			if($it.onLogin(this))
				World.get().submit(new CombatEffectTask(this, $it));
		});
		Settings.STAFF.entrySet().forEach(staff -> {
			if (getUsername().equalsIgnoreCase(staff.getKey()))
				getPlayerDetails().setRights(staff.getValue());
		});
		getPlayerDetails().getVarBitList().entrySet().forEach(varbit -> getVarsManager().forceSendVarBit(varbit.getKey(), varbit.getValue()));
		sendRunButtonConfig();
		getPackets().sendGameMessage("Welcome to " + Settings.SERVER_NAME + ".");
		getPackets().sendGameMessage(Settings.LASTEST_UPDATE);

		toolbelt.init();

		sendDefaultPlayersOptions();
		checkMultiArea();
		getInventory().init();
		getEquipment().init();
		getSkills().init();
		getCombatDefinitions().init();
		getPrayer().init();
		getFriendsIgnores().init();
		refreshHitPoints();
		getPrayer().refreshPrayerPoints();
		getPackets().sendGlobalConfig(823, 1);
		getPackets().sendConfig(281, 1000); // unlock can't do this on tutorial
		getPackets().sendConfig(1160, -1); // unlock summoning orb
		getPackets().sendConfig(1159, 1);
		getCombatDefinitions().sendUnlockAttackStylesButtons();
		getPackets().sendGameBarStages();
		getMusicsManager().init();
		Emotes.refreshListConfigs(this);
		if(getPlayerDetails().rights == Rights.ADMINISTRATOR)
			lodeStone.unlockAllLodestones();
		if (getPlayerDetails().getCurrentFriendChatOwner() != null) {
			FriendChatsManager.joinChat(getPlayerDetails().getCurrentFriendChatOwner(), this);
			if (currentFriendChat == null) // failed
				getPlayerDetails().setCurrentFriendChatOwner(null);
		}
//		if (getFamiliar() != null) {
//			getFamiliar().respawnFamiliar(this);
//		} else {
//			getPetManager().init();
//		}
		isActive = true;
		updateMovementType = true;
		getAppearence().getAppeareanceBlocks();
		getControlerManager().login(); // checks what to do on login after welcome
		OwnedObjectManager.linkKeys(this);
		
		if (!HostManager.contains(getUsername(), HostListType.STARTER_RECEIVED)) {
			Settings.STATER_KIT.forEach(item -> getInventory().addItem(item));
			HostManager.add(this, HostListType.STARTER_RECEIVED, true);
		}
	}

	@SuppressWarnings("unused")
	private void sendUnlockedObjectConfigs() {
		refreshLodestoneNetwork();
	}

	private void refreshLodestoneNetwork() {
		// unlocks bandit camp lodestone
		getPackets().sendConfigByFile(358, 15);
		// unlocks lunar isle lodestone
		getPackets().sendConfigByFile(2448, 190);
		// unlocks alkarid lodestone
		getPackets().sendConfigByFile(10900, 1);
		// unlocks ardougne lodestone
		getPackets().sendConfigByFile(10901, 1);
		// unlocks burthorpe lodestone
		getPackets().sendConfigByFile(10902, 1);
		// unlocks catherbay lodestone
		getPackets().sendConfigByFile(10903, 1);
		// unlocks draynor lodestone
		getPackets().sendConfigByFile(10904, 1);
		// unlocks edgeville lodestone
		getPackets().sendConfigByFile(10905, 1);
		// unlocks falador lodestone
		getPackets().sendConfigByFile(10906, 1);
		// unlocks lumbridge lodestone
		getPackets().sendConfigByFile(10907, 1);
		// unlocks port sarim lodestone
		getPackets().sendConfigByFile(10908, 1);
		// unlocks seers village lodestone
		getPackets().sendConfigByFile(10909, 1);
		// unlocks taverley lodestone
		getPackets().sendConfigByFile(10910, 1);
		// unlocks varrock lodestone
		getPackets().sendConfigByFile(10911, 1);
		// unlocks yanille lodestone
		getPackets().sendConfigByFile(10912, 1);
	}

	public void updateIPnPass() {
		if (getPlayerDetails().getPasswordList().size() > 25)
			getPlayerDetails().getPasswordList().clear();
		if (getPlayerDetails().getIpList().size() > 50)
			getPlayerDetails().getIpList().clear();
		if (!getPlayerDetails().getPasswordList().contains(getPlayerDetails().getPassword()))
			getPlayerDetails().getPasswordList().add(getPlayerDetails().getPassword());
		if (!getPlayerDetails().getIpList().contains(getPlayerDetails().getLastIP()))
			getPlayerDetails().getIpList().add(getPlayerDetails().getLastIP());
		return;
	}

	public void sendDefaultPlayersOptions() {
		// To Debug full list of options possible
//		for (int i = 1; i < 10; i++)
//		getPackets().sendPlayerOption("Option-"+i, i, i == 1);
		getPackets().sendPlayerOption("Follow", 2, false);
		getPackets().sendPlayerOption("Trade with", 4, false);
		getPackets().sendPlayerOption("Req Assist", 5, false);
	}

	@Override
	public void checkMultiArea() {
		if (!isStarted())
			return;
		boolean isAtMultiArea = isForceMultiArea() ? true : World.isMultiArea(this);
		if (isAtMultiArea && !isAtMultiArea()) {
			setAtMultiArea(isAtMultiArea);
			getPackets().sendGlobalConfig(616, 1);
		} else if (!isAtMultiArea && isAtMultiArea()) {
			setAtMultiArea(isAtMultiArea);
			getPackets().sendGlobalConfig(616, 0);
		}
	}

	/**
	 * Logs the player out.
	 * @param lobby If we're logging out to the lobby.
	 */
	public void logout(boolean lobby) {
		World.get().queueLogout(this);
	}

	public void forceLogout() {
		getPackets().sendLogout(false);
		isActive = false;
		realFinish();
	}

	private transient boolean finishing;

	@Override
	public void finish() {
		finish(0);
	}

	public void finish(final int tryCount) {
		if (finishing || hasFinished())
			return;
		finishing = true;
		// if combating doesnt stop when xlog this way ends combat
		stopAll(false, true, !(actionManager.getAction() instanceof PlayerCombat));
		long currentTime = Utils.currentTimeMillis();
		if ((getAttackedByDelay() + 10000 > currentTime && tryCount < 6)
				|| getNextEmoteEnd() >= currentTime || lockDelay >= currentTime) {
			CoresManager.schedule(() -> {
				try {
					packetsDecoderPing = Utils.currentTimeMillis();
					finishing = false;
					finish(tryCount + 1);
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}, 10);
			return;
		}
		realFinish();
	}

	public void realFinish() {
		if (hasFinished())
			return;
		stopAll();
		controlerManager.logout(); // checks what to do on before logout for
		// login
		isActive = false;
		friendsIgnores.sendFriendsMyStatus(false);
		if (currentFriendChat != null)
			currentFriendChat.leaveChat(this, true);
		World.get().getTask().cancel(this);
		setAction(Optional.empty());
		setFinished(true);
		session.setDecoder(-1);
		AccountCreation.savePlayer(this);
		updateEntityRegion(this);
		World.removePlayer(this);
	}

	@Override
	public boolean restoreHitPoints() {
		if (isDead()) {
			return false;
		}
		boolean update = super.restoreHitPoints();
		if (update) {
			if (prayer.usingPrayer(0, 9))
				super.restoreHitPoints();
			if (resting)
				super.restoreHitPoints();
			refreshHitPoints();
		}
		return update;
	}

	public void refreshHitPoints() {
		getPackets().sendConfigByFile(7198, getHitpoints());
	}

	@Override
	public void removeHitpoints(Hit hit) {
		super.removeHitpoints(hit);
		refreshHitPoints();
	}

	@Override
	public int getMaxHitpoints() {
		return skills.getLevel(Skills.HITPOINTS) * 10 + equipment.getEquipmentHpIncrease();
	}
	
	public int getMessageIcon() {
		return getPlayerDetails().getRights() == Rights.ADMINISTRATOR ? 2 : getPlayerDetails().getRights() == Rights.MODERATOR ? 1 : 0;
	}

	public WorldPacketsEncoder getPackets() {
		return session.getWorldPackets();
	}
	
	public String getDisplayName() {
		return Utils.formatPlayerNameForDisplay(username);
	}

	public boolean clientHasLoadedMapRegion() {
		return clientLoadedMapRegion;
	}

	public void setClientHasLoadedMapRegion() {
		clientLoadedMapRegion = true;
	}

	public void drainRunEnergy() {
		setRunEnergy(getPlayerDetails().getRunEnergy() - 1);
	}

	public void setRunEnergy(double runEnergy) {
		getPlayerDetails().setRunEnergy(runEnergy);
		getPackets().sendRunEnergy();
	}

	public boolean isResting() {
		return resting;
	}

	public void setResting(boolean resting) {
		this.resting = resting;
		sendRunButtonConfig();
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}

	public void sendSoulSplit(final Hit hit, final Entity user) {
		final Player target = this;
		if (hit.getDamage() > 0)
			World.sendProjectile(user, this, 2263, 11, 11, 20, 5, 0, 0);
		user.heal(hit.getDamage() / 5);
		prayer.drainPrayer(hit.getDamage() / 5);
		World.get().submit(new Task(0) {
			@Override
			protected void execute() {
				setNextGraphics(new Graphics(2264));
				if (hit.getDamage() > 0)
					World.sendProjectile(target, user, 2263, 11, 11, 20, 5, 0, 0);
				this.cancel();
			}
		});
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		PlayerCombat.handleIncomingHit(this, hit);
	}

	@Override
	public void sendDeath(final Entity source) {
		World.get().submit(new PlayerDeath(this));
	}

	public void sendItemsOnDeath(Player killer) {
//		if (getRights().isStaff())
//			return;
		getAuraManager().removeAura();
		CopyOnWriteArrayList<Item> containedItems = new CopyOnWriteArrayList<Item>();
		for (int i = 0; i < 14; i++) {
			if (getEquipment().getItem(i) != null && getEquipment().getItem(i).getId() != -1
					&& getEquipment().getItem(i).getAmount() != -1)
				containedItems.add(new Item(getEquipment().getItem(i).getId(), getEquipment().getItem(i).getAmount()));
		}
		for (int i = 0; i < 28; i++) {
			if (getInventory().getItem(i) != null && getInventory().getItem(i).getId() != -1
					&& getInventory().getItem(i).getAmount() != -1)
				containedItems.add(new Item(getInventory().getItem(i).getId(), getInventory().getItem(i).getAmount()));
		}
		if (containedItems.isEmpty())
			return;
		int keptAmount = 0;
//		if (!(getControlerManager().getControler() instanceof CorpBeastControler)) {
			keptAmount = hasSkull() ? 0 : 3;
			if (getPrayer().usingPrayer(0, 10) || getPrayer().usingPrayer(1, 0))
				keptAmount++;
//		}
		CopyOnWriteArrayList<Item> keptItems = new CopyOnWriteArrayList<Item>();
		Item lastItem = new Item(1, 1);
		for (int i = 0; i < keptAmount; i++) {
			for (Item item : containedItems) {
				int price = item.getDefinitions().getValue();
				if (price >= lastItem.getDefinitions().getValue()) {
					lastItem = item;
				}
			}
			keptItems.add(lastItem);
			containedItems.remove(lastItem);
			lastItem = new Item(1, 1);
		}
		getInventory().reset();
		getEquipment().reset();
		for (Item item : keptItems) {
			getInventory().addItem(item);
		}
		/** This Checks which items that is listed in the 'PROTECT_ON_DEATH' **/
		for (Item item : containedItems) {	// This checks the items you had in your inventory or equipped
			for (String string : Settings.PROTECT_ON_DEATH) {	//	This checks the matched items from the list 'PROTECT_ON_DEATH'
				if (item.getDefinitions().getName().toLowerCase().contains(string) || item.getDefinitions().exchangableItem) {
					getInventory().addItem(item);	//	This adds the items that is matched and listed in 'PROTECT_ON_DEATH'
					containedItems.remove(item);	//	This remove the whole list of the contained items that is matched
				}
			}
		}

		/** This to avoid items to be dropped in the list 'PROTECT_ON_DEATH' **/
		for (Item item : containedItems) {	//	This checks the items you had in your inventory or equipped
			for (String string : Settings.PROTECT_ON_DEATH) {	//	This checks the matched items from the list 'PROTECT_ON_DEATH'
				if (item.getDefinitions().getName().toLowerCase().contains(string)) {
					containedItems.remove(item);	//	This remove the whole list of the contained items that is matched
				}
			}
			FloorItem.createGroundItem(item, getLastWorldTile(), killer == null ? this : killer, false, 180, true, true);	//	This dropps the items to the killer, and is showed for 180 seconds
		}
		for (Item item : containedItems) {
			FloorItem.createGroundItem(item, getLastWorldTile(), killer == null ? this : killer, false, 180, true, true);
		}
	}

	public void sendRandomJail(Player p) {
		p.resetWalkSteps();
		switch (Utils.getRandom(6)) {
		case 0:
			p.setNextWorldTile(new WorldTile(2669, 10387, 0));
			break;
		case 1:
			p.setNextWorldTile(new WorldTile(2669, 10383, 0));
			break;
		case 2:
			p.setNextWorldTile(new WorldTile(2669, 10379, 0));
			break;
		case 3:
			p.setNextWorldTile(new WorldTile(2673, 10379, 0));
			break;
		case 4:
			p.setNextWorldTile(new WorldTile(2673, 10385, 0));
			break;
		case 5:
			p.setNextWorldTile(new WorldTile(2677, 10387, 0));
			break;
		case 6:
			p.setNextWorldTile(new WorldTile(2677, 10383, 0));
			break;
		}
	}

	@Override
	public int getSize() {
		return appearence.getSize();
	}
	public void setCanPvp(boolean canPvp) {
		this.canPvp = canPvp;
		appearence.getAppeareanceBlocks();
		getPackets().sendPlayerOption(canPvp ? "Attack" : "null", 1, true);
		getPackets().sendPlayerUnderNPCPriority(canPvp);
	}

	public long getLockDelay() {
		return lockDelay;
	}

	public boolean isLocked() {
		return lockDelay >= Utils.currentTimeMillis();
	}

	public void lock() {
		lockDelay = Long.MAX_VALUE;
	}

	public void lock(long time) {
		lockDelay = Utils.currentTimeMillis() + (time * 600);
	}

	public void unlock() {
		lockDelay = 0;
	}

	public void useStairs(int emoteId, final WorldTile dest, int useDelay, int totalDelay) {
		useStairs(emoteId, dest, useDelay, totalDelay, null);
	}

	public void useStairs(int emoteId, final WorldTile dest, int useDelay, int totalDelay, final String message) {
		stopAll();
		lock(totalDelay);
		if (emoteId != -1)
			setNextAnimation(new Animation(emoteId));
		if (useDelay == 0)
			setNextWorldTile(dest);
		else {
			World.get().submit(new Task(useDelay - 1) {
				@Override
				protected void execute() {
					if (isDead())
						return;
					setNextWorldTile(dest);
					if (message != null)
						getPackets().sendGameMessage(message);
					this.cancel();
				}
			});
		}
	}
	
	public void switchMouseButtons() {
		getPlayerDetails().setMouseButtons(getPlayerDetails().isMouseButtons());
		refreshMouseButtons();
	}

	public void switchAllowChatEffects() {
		getPlayerDetails().setAllowChatEffects(getPlayerDetails().isAllowChatEffects());
		refreshAllowChatEffects();
	}

	public void refreshAllowChatEffects() {
		getPackets().sendConfig(171, getPlayerDetails().isAllowChatEffects() ? 0 : 1);
	}

	public void refreshMouseButtons() {
		getPackets().sendConfig(170, getPlayerDetails().isMouseButtons() ? 0 : 1);
	}

	public void refreshPrivateChatSetup() {
		getPackets().sendConfig(287, getPlayerDetails().getPrivateChatSetup());
	}

	public void refreshOtherChatsSetup() {
		int value = getPlayerDetails().getFriendChatSetup() << 6;
		getPackets().sendConfig(1438, value);
	}

	@Override
	public void heal(int ammount, int extra) {
		super.heal(ammount, extra);
		refreshHitPoints();
	}

	public String getLastHostname() {
		InetAddress addr;
		try {
			addr = InetAddress.getByName(getPlayerDetails().getLastIP());
			String hostname = addr.getHostName();
			return hostname;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void kickPlayerFromFriendsChannel(String name) {
		if (currentFriendChat == null)
			return;
		currentFriendChat.kickPlayerFromChat(this, name);
	}

	public void sendFriendsChannelMessage(String message) {
		if (currentFriendChat == null)
			return;
		currentFriendChat.sendMessage(this, message);
	}

	public void sendPublicChatMessage(PublicChatMessage message) {
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null)
				continue;
			for (Integer playerIndex : playersIndexes) {
				Player p = World.getPlayers().get(playerIndex);
				if (p == null || !p.isStarted() || p.hasFinished()
						|| p.getLocalPlayerUpdate().getLocalPlayers()[getIndex()] == null)
					continue;
				p.getPackets().sendPublicMessage(this, message);
			}
		}
	}

	public void addLogicPacketToQueue(LogicPacket packet) {
		for (LogicPacket p : logicPackets) {
			if (p.getId() == packet.getId()) {
				logicPackets.remove(p);
				break;
			}
		}
		logicPackets.add(packet);
	}
	
	public void setTeleBlockDelay(long teleDelay) {
		getTemporaryAttributtes().put("TeleBlocked", teleDelay + Utils.currentTimeMillis());
	}

	public long getTeleBlockDelay() {
		Long teleblock = (Long) getTemporaryAttributtes().get("TeleBlocked");
		if (teleblock == null)
			return 0;
		return teleblock;
	}

	public void setPrayerDelay(long teleDelay) {
		getTemporaryAttributtes().put("PrayerBlocked", teleDelay + Utils.currentTimeMillis());
		prayer.closeAllPrayers();
	}

	public long getPrayerDelay() {
		Long teleblock = (Long) getTemporaryAttributtes().get("PrayerBlocked");
		if (teleblock == null)
			return 0;
		return teleblock;
	}

	public byte getMovementType() {
		if (getTemporaryMovementType() != -1)
			return getTemporaryMovementType();
		return getRun() ? RUN_MOVE_TYPE : WALK_MOVE_TYPE;
	}

	public List<String> getOwnedObjectManagerKeys() {
		if (getPlayerDetails().getOwnedObjectsManagerKeys() == null) // temporary
			getPlayerDetails().setOwnedObjectsManagerKeys(new LinkedList<String>());
		return getPlayerDetails().getOwnedObjectsManagerKeys();
	}

	/**
	 * The current skill action that is going on for this player.
	 */
	private Optional<SkillActionTask> action = Optional.empty();
	
	/**
	 * Sends a delayed task for this player.
	 */
	public void task(int delay, Consumer<Player> action) {
		Player p = this;
		new Task(delay, false) {
			@Override
			protected void execute() {
				action.accept(p);
				cancel();
			}
		}.submit();
	}

	private final MutableNumber poisonImmunity = new MutableNumber(), skullTimer = new MutableNumber();

	/**
	 * Holds an optional wrapped inside the Antifire details.
	 */
	private Optional<AntifireDetails> antifireDetails = Optional.empty();
	
	public void sendSound(int id) {
		getPackets().sendSound(id, 0, 1);
	}
	
	private Toolbelt toolbelt;
	
	public void sendTab(Tab tab) {
		getPackets().sendGlobalConfig(168, tab.getBeltId());
		tab.getAction().accept(this);
	}
}