package com.jupiter.game.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import com.jupiter.game.map.World;
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
@EqualsAndHashCode(callSuper = false)
public class Player extends Entity {

	public transient static final byte TELE_MOVE_TYPE = 127, WALK_MOVE_TYPE = 1, RUN_MOVE_TYPE = 2;

	private transient String username;
	private transient Session session;
	private transient boolean clientLoadedMapRegion = false;
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
	
	/**
	 * Creates a new instance of a Players details
	 */
	private PlayerDetails playerDetails = new PlayerDetails();
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
	private LodeStone lodeStone;
	private Toolbelt toolbelt;
	
	private transient RouteEvent routeEvent;
	private transient Conversation conversation;
	private transient VarManager varsManager;
	private transient ConcurrentLinkedQueue<LogicPacket> logicPackets;
	private transient LocalPlayerUpdate localPlayerUpdate;
	private transient LocalNPCUpdate localNPCUpdate;

	private transient byte temporaryMovementType;
	private transient boolean updateMovementType;
	private transient boolean started;
	private transient boolean isActive;
	private transient long packetsDecoderPing;
	private transient boolean resting;
	private transient boolean canPvp;
	private transient boolean cantTrade;
	private transient Runnable closeInterfacesEvent;
	private transient long lastPublicMessage;
	private transient List<Byte> switchItemCache;
	private transient boolean disableEquip;
	private transient boolean castedVeng;
	private transient double hpBoostMultiplier;
	private transient boolean largeSceneView;
	private transient long nextEmoteEnd;
	private transient boolean forceNextMapLoadRefresh;

	public Player(String password) {
		super(Settings.START_PLAYER_LOCATION, EntityType.PLAYER);
		playerDetails = new PlayerDetails();
		getPlayerDetails().setPassword(password);
	}

	public void init(Session session, String username, byte displayMode, short screenWidth, short screenHeight, IsaacKeyPair isaacKeyPair) {
		this.session = session;
		this.username = username;
		this.displayMode = displayMode;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.isaacKeyPair = isaacKeyPair;
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
		if (auraManager == null)
			auraManager = new AuraManager();
		if(toolbelt == null)
			this.toolbelt = new Toolbelt();
		if (lodeStone == null)
			lodeStone = new LodeStone();
		if (getPlayerDetails().getActivatedLodestones() == null)
			getPlayerDetails().setActivatedLodestones(new boolean[16]);
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

		if (getPlayerDetails().getPasswordList() == null)
			getPlayerDetails().setPasswordList(new ArrayList<String>());
		if (getPlayerDetails().getIpList() == null)
			getPlayerDetails().setIpList(new ArrayList<String>());
		getSession().updateIPnPass(this);
	}

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
			getInterfaceManager().closeInterfaces();
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
		
		if (!(getControlerManager().getControler() instanceof Wilderness) && Wilderness.isAtWild(this)
				&& !Wilderness.isAtWildSafe(this)) {
			getControlerManager().startControler("Wilderness");
		}

		auraManager.process();
		actionManager.process();
		controlerManager.process();
	}

	@Override
	public void setRun(boolean run) {
		if (run != getRun()) {
			super.setRun(run);
			updateMovementType = true;
			getMovement().sendRunButtonConfig();
		}
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
		getMovement().sendRunButtonConfig();
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
		getSkills().refreshHitPoints();
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

	public void sendDefaultPlayersOptions() {
//		IntStream.range(0, 8).forEach(option -> getPackets().sendPlayerOption(""+option, option, false));
		getPackets().sendPlayerOption("Follow", 2, false);
		getPackets().sendPlayerOption("Trade with", 4, false);
		getPackets().sendPlayerOption("Req Assist", 5, false);
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
				|| getNextEmoteEnd() >= currentTime || getMovement().getLockDelay() >= currentTime) {
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
			getSkills().refreshHitPoints();
		}
		return update;
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

	@Override
	public void handleIngoingHit(final Hit hit) {
		PlayerCombat.handleIncomingHit(this, hit);
	}
	
	public void setCanPvp(boolean canPvp) {
		this.canPvp = canPvp;
		appearence.getAppeareanceBlocks();
		getPackets().sendPlayerOption(canPvp ? "Attack" : "null", 1, true);
		getPackets().sendPlayerUnderNPCPriority(canPvp);
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
		getLogicPackets().stream().filter(type -> type.getId() == packet.getId()).forEach(logical -> getLogicPackets().remove(logical));
		getLogicPackets().add(packet);
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
	
	public void sendTab(Tab tab) {
		getPackets().sendGlobalConfig(168, tab.getBeltId());
		tab.getAction().accept(this);
	}
}