package com.jupiter.game.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.jupiter.utility.Utility;

import lombok.Data;

/**
 * A more refined list of Player's personal data (usually primitive objects, no direct classes)
 * @author Dennis
 */
@Data
public final class PlayerDetails {

	/**
	 * Constructs a new Player's personal details
	 */
	public PlayerDetails() {
		pouches = new byte[4];
		maxedCapeCustomized = new int[4];
		completionistCapeCustomized = new int[4];
		varBitList = new ConcurrentHashMap<Integer, Integer>();
		temporaryVarBits = new ConcurrentHashMap<>();
		rights = Rights.PLAYER;
		ownedObjectsManagerKeys = new LinkedList<String>();
		passwordList = new ArrayList<String>();
		ipList = new ArrayList<String>();
		runEnergy = 100D;
		allowChatEffects = true;
		mouseButtons = true;
		activatedLodestones = new boolean[16];
	}

	/**
	 * The players raw Username (non-formatted)
	 */
	private transient String username;
	
	/**
	 * The players personal password for login
	 */
	private String password;
	
	/**
	 * The amount of authority this player has over others.
	 */
	private Rights rights = Rights.PLAYER;
	
	/**
	 * Represents the Public status
	 */
	private byte publicStatus;
	
	/**
	 * Represents the Clan status
	 */
	private byte clanStatus;
	
	/**
	 * Represents the Trade status within an active Trade
	 */
	private transient byte tradeStatus;
	
	/**
	 * A list of activated Lodestones within the world Lodestone Network
	 * (Quick teleporting, requires activation to quickly teleport back there)
	 */
	private boolean[] activatedLodestones;
	
	/**
	 * Represents the Run energy the player can use
	 * to Run. Can manipulated in many ways optionally as well
	 */
	private double runEnergy;
	
	/**
	 * Should the Player use Chat effects for overhead text
	 */
	private boolean allowChatEffects;
	
	/**
	 * Should the Player play with 2 mouse button mode
	 */
	private boolean mouseButtons;
	
	/**
	 * Represents the Private Chat setup
	 */
	private byte privateChatSetup;
	
	/**
	 * Represents the Friends Chat setup
	 */
	private byte friendChatSetup;

	/**
	 * A list of Passwords from the Player. Passwords are currently hashed
	 * so no real point in storing. However it's here anyways Also good for
	 * finding duplicate account users.
	 */
	private ArrayList<String> passwordList = new ArrayList<String>();
	
	/**
	 * A list of IP's logged from the player, useful for keeping track
	 * of any abuse-based players making it easier to track their accounts, etc...
	 */
	private ArrayList<String> ipList = new ArrayList<String>();

	/**
	 * Represents the currently Friends Chat to join
	 */
	private String currentFriendChatOwner;
	
	/**
	 * The quick-selected option id for the summoning orb.
	 */
	private byte summoningLeftClickOption;
	
	/**
	 * A list of Player owned objects (like Hunter box traps, Dwarf Multi cannon, etc..)
	 */
	private List<String> ownedObjectsManagerKeys;
	
	/**
	 * An array of Runecrafting pouches that possibly contain values.
	 */
	private byte[] pouches;

	/**
	 * Represents the total play time of a Player since account creation
	 */
	private long displayTime;

	/**
	 * Adds Players display time
	 * @param time
	 */
	public void addDisplayTime(long time) {
		this.displayTime = time + Utility.currentTimeMillis();
	}
	
	/**
	 * The length of a Player being Muted (Unable to chat)
	 */
	private long muted;

	/**
	 * Length of the Player being Jailed (stuck in a remote area)
	 */
	private long jailed;

	/**
	 * Represents the last known IP from the Player
	 */
	private String lastIP;

	/**
	 * Represents if a Player is filtering out their chatbox messages
	 */
	private boolean filterGame;
	
	/**
	 * Represents if the Player has their experience locked
	 */
	private boolean xpLocked;

	/**
	 * An array of possible changes to the Max Cape customization
	 */
	private int[] maxedCapeCustomized;
	
	/**
	 * An array of possible changes to the Completionist Cape customization
	 */
	private int[] completionistCapeCustomized;

	/**
	 * Represents if the Player should be using older item models to display
	 */
	private boolean oldItemsLook;
	
	/**
	 * Represents the default Yell color for a Player
	 */
	private String yellColor = "ff0000";

	/**
	 * Represents the Skull (over head) type (colors)
	 */
	private int skullId;

	/**
	 * A list of VarBits (Object transformation) ID & their values.
	 * You can right click any object to see if a varbit isn't equal to -1,
	 * if the object has a key value greater than -1 then use 0-5 to debug
	 * transformation types. (Usually 0-3 in most cases)
	 */
	private ConcurrentHashMap<Integer, Integer> varBitList = new ConcurrentHashMap<>();
	
	/**
	 * A list of temporary VarBits that ARE NOT meant to be saved to a player but simply used on demand
	 */
	private transient ConcurrentHashMap<Integer, Integer> temporaryVarBits = new ConcurrentHashMap<>();
}