package com.jupiter.game.player;

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
import com.jupiter.game.player.activity.Activity;
import com.jupiter.game.player.activity.ActivityHandler;
import com.jupiter.game.player.activity.impl.WildernessActivity;
import com.jupiter.game.player.attributes.Attribute;
import com.jupiter.game.player.attributes.AttributeMap;
import com.jupiter.game.player.content.AuraManager;
import com.jupiter.game.player.content.Emotes;
import com.jupiter.game.player.content.FriendChatsManager;
import com.jupiter.game.player.content.LodeStone;
import com.jupiter.game.player.content.MusicsManager;
import com.jupiter.game.player.content.PriceCheckManager;
import com.jupiter.game.player.content.Toolbelt;
import com.jupiter.game.route.CoordsEvent;
import com.jupiter.game.route.strategy.RouteEvent;
import com.jupiter.game.task.Task;
import com.jupiter.game.task.impl.CombatEffectTask;
import com.jupiter.game.task.impl.SkillActionTask;
import com.jupiter.network.Session;
import com.jupiter.network.decoders.LogicPacket;
import com.jupiter.network.decoders.WorldPacketsDecoder;
import com.jupiter.network.encoders.WorldPacketsEncoder;
import com.jupiter.network.encoders.other.HintIconsManager;
import com.jupiter.network.encoders.other.Hit;
import com.jupiter.network.encoders.other.LocalNPCUpdate;
import com.jupiter.network.encoders.other.LocalPlayerUpdate;
import com.jupiter.network.encoders.other.PublicChatMessage;
import com.jupiter.network.host.HostListType;
import com.jupiter.network.host.HostManager;
import com.jupiter.network.utility.IsaacKeyPair;
import com.jupiter.skills.Skills;
import com.jupiter.skills.prayer.Prayer;
import com.jupiter.skills.prayer.PrayerManager;
import com.jupiter.utility.LogUtility;
import com.jupiter.utility.LogUtility.Type;
import com.jupiter.utility.MutableNumber;
import com.jupiter.utility.Utility;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Player extends Entity {

	protected transient String username;
	protected transient Session session;
	protected transient boolean clientLoadedMapRegion = false;
	protected transient byte displayMode;
	protected transient short screenWidth;
	protected transient short screenHeight;
	protected transient InterfaceManager interfaceManager;
	protected transient HintIconsManager hintIconsManager;
	protected transient ActionManager actionManager;
	protected transient PriceCheckManager priceCheckManager;
	protected transient CoordsEvent coordsEvent;
	protected transient FriendChatsManager currentFriendChat;
	protected transient Trade trade;
	protected transient IsaacKeyPair isaacKeyPair;
	protected transient RouteEvent routeEvent;
	protected transient Conversation conversation;
	protected transient VarManager varsManager;
	protected transient ConcurrentLinkedQueue<LogicPacket> logicPackets;
	protected transient LocalPlayerUpdate localPlayerUpdate;
	protected transient LocalNPCUpdate localNPCUpdate;
	
	protected PlayerDetails playerDetails = new PlayerDetails();
	protected AttributeMap<Attribute> attributes = new AttributeMap<>(Attribute.class);
	protected Appearance appearence;
	protected Inventory inventory;
	protected Equipment equipment;
	protected Skills skills;
	protected CombatDefinitions combatDefinitions;
	protected PrayerManager prayer;
	protected Bank bank;
	protected MusicsManager musicsManager;
	protected FriendsIgnores friendsIgnores;
	protected AuraManager auraManager;
	protected LodeStone lodeStone;
	protected Toolbelt toolbelt;
	
	/**
	 * The current activity this Player is in.
	 */
	private Optional<Activity> currentActivity;
	
	/**
	 * The current skill action that is going on for this player.
	 */
	private Optional<SkillActionTask> action = Optional.empty();

	private final MutableNumber poisonImmunity = new MutableNumber(), skullTimer = new MutableNumber();

	/**
	 * Holds an optional wrapped inside the Antifire details.
	 */
	private Optional<AntifireDetails> antifireDetails = Optional.empty();

	private transient boolean started;
	private transient boolean isActive;
	private transient boolean resting;
	private transient boolean canPvp;
	private transient boolean cantTrade;
	private transient Runnable closeInterfacesEvent;
	private transient long lastPublicMessage;
	private transient boolean disableEquip;
	private transient boolean castedVeng;
	private transient double hpBoostMultiplier;
	private transient boolean largeSceneView;
	private transient long nextEmoteEnd;
	private transient boolean forceNextMapLoadRefresh;
	protected transient List<Byte> switchItemCache;
	protected transient long packetsDecoderPing;
	protected transient byte temporaryMovementType;
	protected transient boolean updateMovementType;
	
	public Player(String password) {
		super(Settings.START_PLAYER_LOCATION, EntityType.PLAYER);
		playerDetails = new PlayerDetails();
		getPlayerDetails().setPassword(password);
	}

	public void start() {
		loadMapRegions();
		started = true;
		run();
		if (isDead())
			sendDeath(null);
	}
	
	public void startConversation(Dialogue dialogue) {
		startConversation(new Conversation(dialogue.finish()));
	}

	public void startConversation(Conversation conversation) {
		if (conversation.getCurrent() == null)
			return;
		this.conversation = conversation;
		this.conversation.setPlayer(this);
		conversation.start();
	}

	public void endConversation() {
		this.conversation = null;
		if (getInterfaceManager().containsChatBoxInter())
			getInterfaceManager().closeChatBoxInterface();
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
		
		if (!(getCurrentActivity().get() instanceof WildernessActivity) && WildernessActivity.isInideWilderness(this)) {
			ActivityHandler.startActivity(this, new WildernessActivity());
		}
		auraManager.process();
		actionManager.process();
		ActivityHandler.executeVoid(this, activity -> activity.process(this));
	}
	
	public void run() {
		if (World.exiting_start != 0) {
			int delayPassed = (int) ((Utility.currentTimeMillis() - World.exiting_start) / 1000);
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
		getPrayer().refreshPoints();
		getPackets().sendGlobalConfig(823, 1);
		getPackets().sendConfig(281, 1000); // unlock can't do this on tutorial
		getPackets().sendConfig(1160, -1); // unlock summoning orb
		getPackets().sendConfig(1159, 1);
		getCombatDefinitions().sendUnlockAttackStylesButtons();
		getPackets().sendGameBarStages();
		getMusicsManager().init();
		Emotes.refreshListConfigs(this);
		if(getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			lodeStone.unlockAllLodestones();
		if (getPlayerDetails().getCurrentFriendChatOwner() != null) {
			FriendChatsManager.joinChat(getPlayerDetails().getCurrentFriendChatOwner(), this);
			if (currentFriendChat == null) // failed
				getPlayerDetails().setCurrentFriendChatOwner(null);
		}
		ActivityHandler.executeVoid(this, activity -> activity.login(this));
		isActive = true;
		updateMovementType = true;
		getAppearence().getAppeareanceBlocks();
		
		OwnedObjectManager.linkKeys(this);
		
		if (!HostManager.contains(getUsername(), HostListType.STARTER_RECEIVED)) {
			Settings.STATER_KIT.forEach(getInventory()::addItem);
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
		getAttributes().stopAll(this, false, true, !(actionManager.getAction() instanceof PlayerCombat));
		long currentTime = Utility.currentTimeMillis();
		if ((getAttackedByDelay() + 10000 > currentTime && tryCount < 6)
				|| getNextEmoteEnd() >= currentTime || getMovement().getLockDelay() >= currentTime) {
			CoresManager.schedule(() -> {
				try {
					packetsDecoderPing = Utility.currentTimeMillis();
					finishing = false;
					finish(tryCount + 1);
				} catch (Throwable e) {
					LogUtility.log(Type.ERROR, "Player", e.getMessage());
				}
			}, 10);
			return;
		}
		realFinish();
	}

	public void realFinish() {
		if (hasFinished())
			return;
		getAttributes().stopAll(this);
		ActivityHandler.executeVoid(this, activity -> activity.logout(this));
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
		int toRegen = 0;
		if (isResting())
			toRegen += 1;
		if (getPrayer().active(Prayer.RAPID_HEAL))
			toRegen += 1;
		if (getPrayer().active(Prayer.RAPID_RENEWAL))
			toRegen += 4;
		if (getEquipment().getGlovesId() == 11133)
			toRegen *= 2;
		if ((getHitpoints() + toRegen) > getMaxHitpoints())
			toRegen = getMaxHitpoints() - getHitpoints();
		if (getHitpoints() < getMaxHitpoints())
			setHitpoints(getHitpoints() + toRegen);
		if (update || toRegen > 0)
			getSkills().refreshHitPoints();
		return update;
	}
	
	public int getMessageIcon() {
		return getPlayerDetails().getRights() == Rights.ADMINISTRATOR ? 2 : getPlayerDetails().getRights() == Rights.MODERATOR ? 1 : 0;
	}

	public WorldPacketsEncoder getPackets() {
		return session.getWorldPackets();
	}
	
	public String getDisplayName() {
		return Utility.formatPlayerNameForDisplay(username);
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
		getTemporaryAttributtes().put("TeleBlocked", teleDelay + Utility.currentTimeMillis());
	}

	public long getTeleBlockDelay() {
		Long teleblock = (Long) getTemporaryAttributtes().get("TeleBlocked");
		if (teleblock == null)
			return 0;
		return teleblock;
	}

	public void setPrayerDelay(long teleDelay) {
		getTemporaryAttributtes().put("PrayerBlocked", teleDelay + Utility.currentTimeMillis());
		prayer.closeAllPrayers();
	}

	public long getPrayerDelay() {
		Long teleblock = (Long) getTemporaryAttributtes().get("PrayerBlocked");
		if (teleblock == null)
			return 0;
		return teleblock;
	}

	public List<String> getOwnedObjectManagerKeys() {
		if (getPlayerDetails().getOwnedObjectsManagerKeys() == null) // temporary
			getPlayerDetails().setOwnedObjectsManagerKeys(new LinkedList<String>());
		return getPlayerDetails().getOwnedObjectsManagerKeys();
	}
	
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
	
	public void sendTab(Tab tab) {
		getPackets().sendGlobalConfig(168, tab.getBeltId());
//		tab.getAction().accept(this);
	}
}