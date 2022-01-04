package com.jupiter.game.player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import com.jupiter.Settings;
import com.jupiter.cache.io.InputStream;
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
import com.jupiter.game.route.RouteEvent;
import com.jupiter.game.task.Task;
import com.jupiter.game.task.impl.CombatEffectTask;
import com.jupiter.game.task.impl.SkillActionTask;
import com.jupiter.network.Session;
import com.jupiter.network.decoders.LogicPacket;
import com.jupiter.network.encoders.WorldPacketsEncoder;
import com.jupiter.network.encoders.other.HintIconsManager;
import com.jupiter.network.encoders.other.Hit;
import com.jupiter.network.encoders.other.LocalNPCUpdate;
import com.jupiter.network.encoders.other.LocalPlayerUpdate;
import com.jupiter.network.encoders.other.PublicChatMessage;
import com.jupiter.network.host.HostListType;
import com.jupiter.network.host.HostManager;
import com.jupiter.network.packets.logic.LogicPacketDispatcher;
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

	/**
	 * The Player's Session
	 */
	private transient Session session;
	
	/**
	 * The Client Region Map Loading state
	 */
	private transient boolean clientLoadedMapRegion;
	
	/**
	 * Display Mode Type (Fixed, Resize, Fullscreen)
	 */
	private transient byte displayMode;
	
	/**
	 * Client Screen Width
	 */
	private transient short screenWidth;
	
	/**
	 * Client Screen Height
	 */
	private transient short screenHeight;
	
	/**
	 * Designed to help prevent Packet Injection
	 */
	private transient IsaacKeyPair isaacKeyPair;
	
	/**
	 * Represents a Player's Interface management system
	 */
	private transient InterfaceManager interfaceManager;
	
	/**
	 * Represents a Player's Hint Icon management system
	 */
	private transient HintIconsManager hintIconsManager;
	
	/**
	 * Represents a Player's Price Checker's system
	 */
	private transient PriceCheckManager priceCheckManager;
	
	/**
	 * Represents a Player's Route (movement) management system
	 */
	private transient RouteEvent routeEvent;
	
	/**
	 * Represents a Player's current Friends Chat (FC)
	 */
	private transient FriendChatsManager currentFriendChat;
	
	/**
	 * Represents a Player's appearance management system
	 */
	private Appearance appearance;
	
	/**
	 * Represents a Player's inventory management system
	 */
	private Inventory inventory;
	
	/**
	 * Represents a Player's Equipment management system
	 */
	private Equipment equipment;
	
	/**
	 * Represents a Player's Skills management system
	 */
	private Skills skills;
	
	/**
	 * Represents a Player's Combat Definitions management system
	 */
	private CombatDefinitions combatDefinitions;
	
	/**
	 * Represents a Action management system
	 */
	private transient ActionManager actionManager;
	
	/**
	 * Represents a Player's coordinate (movement) management system
	 */
	private transient CoordsEvent coordsEvent;
	
	/**
	 * Represents a Player's Trade system
	 */
	private transient Trade trade;
	
	/**
	 * Represents an instance of a conversation for the target Player
	 */
	private transient Conversation conversation;
	
	/**
	 * Represents a Player's Vars management system
	 */
	private transient VarManager varsManager;
	
	/**
	 * Represents a Player's queue logic packets listing
	 */
	private transient ConcurrentLinkedQueue<LogicPacket> logicPackets;
	
	/**
	 * Represents the Player updating masks
	 */
	private transient LocalPlayerUpdate localPlayerUpdate;
	
	/**
	 * Represents the NPC updating masks
	 */
	private transient LocalNPCUpdate localNPCUpdate;
	
	/**
	 * Personal details & information stored for a Player
	 */
	private PlayerDetails playerDetails = new PlayerDetails();
	
	/**
	 * Represents the type-safe attributes of a Player
	 */
	private AttributeMap<Attribute> attributes = new AttributeMap<>(Attribute.class);
	
	/**
	 * Represents a Player's Prayer management system
	 */
	private PrayerManager prayer;
	
	/**
	 * Represents a Player's Bank management system
	 */
	private Bank bank;
	
	/**
	 * Represents a Player's Music management system
	 */
	private MusicsManager musicsManager;
	
	/**
	 * Represents a Player's Friends Ignore management system
	 */
	private FriendsIgnores friendsIgnores;
	
	/**
	 * Represents a Player's Aura handler
	 */
	private AuraManager auraManager;
	
	/**
	 * Represents a Player's Lodestone network
	 */
	private LodeStone lodeStone;
	
	/**
	 * Represents a Player's toolbelt (Items stored in a hidden backpack-like system)
	 */
	private Toolbelt toolbelt;
	
	/**
	 * The current activity this Player is in.
	 */
	private Optional<Activity> currentActivity = Optional.empty();
	
	/**
	 * The current skill action that is going on for this player.
	 */
	private Optional<SkillActionTask> action = Optional.empty();

	private final MutableNumber poisonImmunity = new MutableNumber(), skullTimer = new MutableNumber();

	/**
	 * Holds an optional wrapped inside the Antifire details.
	 */
	private Optional<AntifireDetails> antifireDetails = Optional.empty();

	/**
	 * Represents a Movement type
	 */
	private transient byte temporaryMovementType;
	
	/**
	 * Should we update the Movement state
	 */
	private transient boolean updateMovementType;
	
	/**
	 * Has the Player started their {@link #session}
	 */
	private transient boolean started;
	
	/**
	 * Is the Player's {@link #session} currently Running
	 */
	private transient boolean running;
	
	/**
	 * The type of Resting state of a Player
	 * Example: Not, Sitting, idle
	 */
	private transient boolean resting;
	
	/**
	 * Can the Player engage in PVP combat
	 */
	private transient boolean canPvp;
	
	/**
	 * Does the Player have permission to Trade others
	 */
	private transient boolean cantTrade;
	
	/**
	 * Represents a Runnable event that takes place when closing an interface
	 */
	private transient Runnable closeInterfacesEvent;
	
	/**
	 * The last known time length of the last message a Player sends
	 */
	private transient long lastPublicMessage;
	
	/**
	 * The Item switching cache (Switches for PVE/PVP)
	 */
	private transient List<Byte> switchItemCache;
	
	/**
	 * Does the Player have their Equipping/Removing disabled
	 */
	private transient boolean disableEquip;
	
	/**
	 * Does the Player become invulnerable to any damage.
	 */
	private transient boolean invulnerable;
	
	/**
	 * Is the Player finishing their {@link #session}
	 */
	private transient boolean finishing;
	
	/**
	 * Represents the current game state of the Player
	 */
	private transient boolean isActive;
	
	/**
	 * Has the Player casted their veng spell
	 * NOTE: This'll be removed, and supported through attributes(
	 */
	private transient boolean castedVeng;
	
	/**
	 * Represents the Player's HP boost multiplier (Bonfire, etc..)
	 */
	private transient double hpBoostMultiplier;
	
	/**
	 * Represents the next Map state
	 */
	private transient boolean forceNextMapLoadRefresh;
	
	/**
	 * Represents a Player's client POV state
	 */
	private transient boolean largeSceneView;
	
	/**
	 * Represents a Player's last Emote delay (used for various things)
	 */
	private transient long nextEmoteEnd;
	
	/**
	 * Creates a new Player
	 * @param password
	 */
	public Player(String password) {
		super(Settings.START_PLAYER_LOCATION, EntityType.PLAYER);
		playerDetails = new PlayerDetails();
		getPlayerDetails().setPassword(password);
	}

	/**
	 * Loads the Player into the game world, further processes other functions as well.
	 */
	public void start() {
		loadMapRegions();
		started = true;
		run();
		if (isDead())
			sendDeath(null);
	}
	
	/**
	 * Starts a Dialogue
	 * @param dialogue
	 */
	public void startConversation(Dialogue dialogue) {
		startConversation(new Conversation(dialogue.finish()));
	}

	/**
	 * Starts a Conversation
	 * @param dialogue
	 */
	public void startConversation(Conversation conversation) {
		if (conversation.getCurrent() == null)
			return;
		this.conversation = conversation;
		this.conversation.setPlayer(this);
		conversation.start();
	}

	/**
	 * Ends a conversation
	 */
	public void endConversation() {
		if (conversation != null)
			this.conversation = null;
		if (getInterfaceManager().containsChatBoxInter())
			getInterfaceManager().closeChatBoxInterface();
	}

	/**
	 * Processes the Logic-based packets to the game network
	 */
	public void processLogicPackets(Player player) {
		LogicPacket packet;
		while ((packet = player.getLogicPackets().poll()) != null) {
			InputStream stream = new InputStream(packet.getData());
			LogicPacketDispatcher.execute(player, stream, packet.getId());
		}
	}
	
	/**
	 * Processes the Player itself within the game network
	 */
	@Override
	public void processEntity() {
		if (isDead() || !isActive()) {
			return;
		}
		if (finishing)
			finish(0);
		processLogicPackets(this);
		super.processEntity();
		if (coordsEvent != null && coordsEvent.processEvent(this))
			coordsEvent = null;
		if (routeEvent != null && routeEvent.processEvent(this))
			routeEvent = null;
		
		if (musicsManager.musicEnded())
			musicsManager.replayMusic();
		
		if (getCurrentActivity().isPresent() && getCurrentActivity().get() instanceof WildernessActivity && WildernessActivity.isInideWilderness(this)) {
			ActivityHandler.startActivity(this, new WildernessActivity());
		}
		auraManager.process();
		actionManager.process();
		ActivityHandler.executeVoid(this, activity -> activity.process(this));
	}
	
	/**
	 * Sends all essential data when a Player logs in
	 */
	public void run() {
		if (World.exiting_start != 0) {
			int delayPassed = (int) ((Utility.currentTimeMillis() - World.exiting_start) / 1000);
			getPackets().sendSystemUpdate(World.exiting_delay - delayPassed);
		}
		
		getAppearance().generateAppearenceData();
		getPlayerDetails().setLastIP(getSession().getIP());
		getInterfaceManager().sendInterfaces();
		getPackets().sendRunEnergy();
		getPackets().sendItemsLook();
		getPackets().sendConfig(171, getPlayerDetails().isAllowChatEffects() ? 0 : 1);
		getPackets().sendConfig(170, getPlayerDetails().isMouseButtons() ? 0 : 1);
		getPackets().sendConfig(287, getPlayerDetails().getPrivateChatSetup());
		getPackets().sendConfig(1438, getPlayerDetails().getFriendChatSetup() << 6);
		CombatEffect.values().forEach($it -> {
			if($it.onLogin(this))
				World.get().submit(new CombatEffectTask(this, $it));
		});
		Settings.STAFF.entrySet().parallelStream().filter(p -> getPlayerDetails().getUsername().equalsIgnoreCase(p.getKey())).forEach(staff -> getPlayerDetails().setRights(staff.getValue()));
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
		getAppearance().getAppeareanceBlocks();
		
		OwnedObjectManager.linkKeys(this);
		
		if (!HostManager.contains(getPlayerDetails().getUsername(), HostListType.STARTER_RECEIVED)) {
			Settings.STATER_KIT.forEach(getInventory()::addItem);
			HostManager.add(this, HostListType.STARTER_RECEIVED, true);
		}
	}

	/**
	 * Sets the default options for another Player's right click option
	 */
	public void sendDefaultPlayersOptions() {
//		IntStream.range(0, 8).forEach(option -> getPackets().sendPlayerOption(""+option, option, false));
		getPackets().sendPlayerOption("Follow", 2, false);
		getPackets().sendPlayerOption("Trade with", 4, false);
		getPackets().sendPlayerOption("Req Assist", 5, false);
	}

	/**
	 * Attempts to finish game session
	 */
	@Override
	public void finish() {
		finish(0);
	}

	/**
	 * Attempts to Finish game session
	 */
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

	/**
	 * Final session closing & player removal from game network
	 */
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

	/**
	 * Restores the Player's Hitpoints
	 */
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
	
	/**
	 * Represents the Player's chatbox message icon ID
	 * @return icon
	 */
	public int getMessageIcon() {
		return getPlayerDetails().getRights() == Rights.ADMINISTRATOR ? 2 : getPlayerDetails().getRights() == Rights.MODERATOR ? 1 : 0;
	}

	/**
	 * Gets the Players Encoder-based packets
	 * @return packets
	 */
	public WorldPacketsEncoder getPackets() {
		return session.getWorldPackets();
	}
	
	/**
	 * Gets the pretty-printed version of a Player's Username
	 * @return
	 */
	public String getDisplayName() {
		return Utility.formatPlayerNameForDisplay(getPlayerDetails().getUsername());
	}

	/**
	 * Handles in incoming hit to the Player
	 */
	@Override
	public void handleIngoingHit(final Hit hit) {
		PlayerCombat.handleIncomingHit(this, hit);
	}

	/**
	 * Adds logic-based packets to a queue
	 * @param packet
	 */
	public void addLogicPacketToQueue(LogicPacket packet) {
		getLogicPackets().stream().filter(type -> type.getId() == packet.getId()).forEach(logical -> getLogicPackets().remove(logical));
		getLogicPackets().add(packet);
	}
	
	//These will be someday converted to the new attribute system.
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
	
	/**
	 * Opens a specific tab, with optional event support
	 * @param tab
	 */
	public void sendTab(Tab tab) {
		getPackets().sendGlobalConfig(168, tab.getBeltId());
		tab.getAction().accept(this);
	}

	/**
	 * Gets the Weight of the total weight of the Player
	 * (Inventory & Equipment calculated together)
	 * @return total weight
	 */
	public double getWeight() {
		return getInventory().getInventoryWeight() + getEquipment().getEquipmentWeight();
	}
}